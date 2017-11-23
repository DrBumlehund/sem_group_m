package m.group.sem.projectm.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;
import java.util.ArrayList;

import Model.Report;
import Model.User;
import Model.UserComment;
import Model.Vote;
import cz.msebera.android.httpclient.Header;
import m.group.sem.projectm.Adapters.ReportCommentsAdapter;
import m.group.sem.projectm.Constants;
import m.group.sem.projectm.R;
import m.group.sem.projectm.Utilities;

public class ViewReportFragment extends Fragment {
    private Report mReport;
    private EditText mCommentInput;
    private ImageButton mUpvote;
    private ImageButton mDownvote;
    private TextView mVoteNumber;
    private TextView mDescription;
    private User mUser;

    private AsyncHttpClient mHttpClient = new AsyncHttpClient();
    private ObjectMapper mMapper = new ObjectMapper();
    private boolean isVoting = false;
    private boolean isCommenting = false;
    private RecyclerView mCommentsRecycler;
    private ReportCommentsAdapter mAdapter;
    private ImageButton mSendComment;

    public ViewReportFragment() {
        // Required empty public constructor
    }

    public static ViewReportFragment newInstance(String param1, String param2) {
        ViewReportFragment fragment = new ViewReportFragment();
        return fragment;
    }

    public void setReport(Report report, Boolean containsDetails) {
        setReport(report);
        if (!containsDetails) {
            // TODO: start spinner or let the user know we're fetching details
            fetchReportDetail(report.getId());
        }
    }

    private void fetchReportDetail(int reportId) {
        String url = Constants.getBaseUrl() + String.format("/reports?report-id=%1$s", reportId);
        mHttpClient.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    Report report = mMapper.readValue(responseBody, Report.class);
                    setReport(report);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to get report details!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getContext(), "Failed to get report details!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setReport(Report report) {
        mReport = report;
        ArrayList<Vote> votes = report.getVotes();
        Vote userVote = null;
        int voteNumber = 0;
        for (Vote vote : votes) {
            voteNumber += vote.isUpvote() ? 1 : -1;
            if (vote.getUserId() == mUser.getId()) {
                userVote = vote;
            }
        }

        if (userVote != null) {
            if (userVote.isUpvote()) {
                mUpvote.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent));
                mDownvote.setColorFilter(ContextCompat.getColor(getContext(), R.color.black));
            } else {
                mUpvote.setColorFilter(ContextCompat.getColor(getContext(), R.color.black));
                mDownvote.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent));
            }
        }

        mCommentInput.setText("");
        mVoteNumber.setText(voteNumber + "");
        mDescription.setText(report.getComment());

        mAdapter = new ReportCommentsAdapter(mReport.getUserComments());
        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String serializedUser = sharedPref.getString(getString(R.string.i_user), null);
        if (serializedUser != null) {
            try {
                mUser = (User) Utilities.fromString(serializedUser);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(this.getClass().toString(), "No user in preferences! Cannot vote or comment if we don't know the user.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_report, container, false);
        mCommentInput = view.findViewById(R.id.comment_input);
        mUpvote = view.findViewById(R.id.upvote);
        mDownvote = view.findViewById(R.id.downvote);
        mSendComment = view.findViewById(R.id.send_comment);
        mVoteNumber = view.findViewById(R.id.vote_number);
        mDescription = view.findViewById(R.id.description);
        mCommentsRecycler = view.findViewById(R.id.commentsRecycler);

        mUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveVote(true);
            }
        });
        mDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveVote(false);
            }
        });

        mCommentInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return saveComment(textView.getText().toString());
            }
        });
        mSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveComment(mCommentInput.getText().toString());
            }
        });

        // Test code
//        Vote userVote = new Vote();
//        userVote.setUpvote(true);
//        userVote.setUserId(mUser.getId());
        Report report = new Report();
        report.setId(1);
        report.setComment("There was an issue when I turned the corner on whatever street!");
        setReport(report);
        /// Test code
        return view;
    }

    private boolean saveComment(String comment) {
        boolean valid = validateComment(comment);
        if (!valid || isCommenting) {
            return false;
        }

        disableComment();
        String url = Constants.getBaseUrl() + String.format("/comments?report-id=%1$s&comment=%2$s&user-id=%3$s", mReport.getId(), comment, mUser.getId());
        mHttpClient.post(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                UserComment userComment = null;
                try {
                    userComment = mMapper.readValue(responseBody, UserComment.class);
                    userComment.setUsername(mUser.getUsername());

                    mReport.getUserComments().add(userComment);
                    mAdapter.notifyItemInserted(mReport.getUserComments().size() - 1);
                    mCommentInput.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to save comment!", Toast.LENGTH_SHORT).show();
                } finally {
                    enableComment();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getContext(), "Failed to save comment!", Toast.LENGTH_SHORT).show();
                enableComment();
            }
        });

        return true;
    }

    private boolean validateComment(String comment) {
        return !(comment == null || comment.isEmpty());
    }

    private void saveVote (final boolean upvote) {
        if (isVoting == false) {
            disableVote();
            String url = Constants.getBaseUrl() + String.format("/votes?report-id=%1$s&vote=%2$s&user-id=%3$s", mReport.getId(), upvote, mUser.getId());
            mHttpClient.post(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Vote vote = null;
                    try {
                        vote = mMapper.readValue(responseBody, Vote.class);

                        ArrayList<Vote> loopyCachyVotes = new ArrayList<>(mReport.getVotes());
                        for (Vote userVote : loopyCachyVotes) {
                            if (userVote.getUserId() == mUser.getId()) {
                                mReport.getVotes().remove(userVote);
                            }
                        }

                        mReport.getVotes().add(vote);

                        if (upvote) {
                            mUpvote.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent));
                            mDownvote.setColorFilter(ContextCompat.getColor(getContext(), R.color.black));
                        } else {
                            mUpvote.setColorFilter(ContextCompat.getColor(getContext(), R.color.black));
                            mDownvote.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent));
                        }

                        int voteNumber = 0;
                        for (Vote _vote : mReport.getVotes()) {
                            voteNumber += vote.isUpvote() ? 1 : -1;
                        }
                        mVoteNumber.setText(voteNumber + "");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Failed to save vote!", Toast.LENGTH_SHORT).show();
                    } finally {
                        enableVote();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getContext(), "Failed to save vote!", Toast.LENGTH_SHORT).show();
                    enableVote();
                }
            });
        }
    }

    private void disableVote() {
        mUpvote.setEnabled(false);
        mDownvote.setEnabled(false);
        isVoting = true;
    }

    private void enableVote() {
        mUpvote.setEnabled(true);
        mDownvote.setEnabled(true);
        isVoting = false;
    }

    private void disableComment() {
        mCommentInput.setEnabled(false);
        mSendComment.setEnabled(false);
        isCommenting = true;
    }

    private void enableComment() {
        mCommentInput.setEnabled(true);
        mSendComment.setEnabled(true);
        isCommenting = false;
    }


}
