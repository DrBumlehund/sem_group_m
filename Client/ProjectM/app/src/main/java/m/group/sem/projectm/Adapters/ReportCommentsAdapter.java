package m.group.sem.projectm.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import Model.UserComment;
import m.group.sem.projectm.R;

/**
 * Created by Simon on 19-11-2017.
 */

public class ReportCommentsAdapter extends RecyclerView.Adapter<ReportCommentsAdapter.ViewHolder> {

    private final ArrayList<UserComment> mComments;

    public ReportCommentsAdapter(ArrayList<UserComment> comments) {
        mComments = comments;
    }

    @Override
    public ReportCommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(parent.getContext());
        View view = li.inflate(R.layout.row_comment, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportCommentsAdapter.ViewHolder holder, int position) {
        UserComment comment = mComments.get(position);
        holder.usernameText.setText(comment.getUsername());
        holder.commentText.setText(comment.getComment());
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView usernameText;
        private final TextView commentText;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username);
            commentText = itemView.findViewById(R.id.comment);
        }
    }
}
