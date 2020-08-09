package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.HashTag;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.ui.activities.TagSearchActivity;
import dynasty.software.the.stylishly.ui.base.BaseViewHolder;

/**
 * Author : Aduraline.
 */

public class HashTagsListAdapter extends RecyclerView.Adapter<HashTagsListAdapter.HashTagListViewHolder> {

    private List<HashTag> mHashTags = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public HashTagsListAdapter(Context context, List<HashTag> tags) {
        mHashTags = tags;
        mContext = context;

        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public HashTagListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mContext == null)
            mContext = parent.getContext();

        return new HashTagListViewHolder(mLayoutInflater.inflate(R.layout.layout_hash_tag, parent, false));
    }

    @Override
    public void onBindViewHolder(HashTagListViewHolder holder, int position) {

        final HashTag hashTag = mHashTags.get(position);
        holder.tagNameTextView.setText(hashTag.string());

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, TagSearchActivity.class);
                intent.putExtra(TagSearchActivity.TAG_KEY, hashTag.name);
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return mHashTags.size();
    }

    class HashTagListViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_tag_name_hash_tag_layout)
        TextView tagNameTextView;
        @BindView(R.id.layout_hash_tag_root)
        RelativeLayout root;

        public HashTagListViewHolder(View itemView) {
            super(itemView);
        }
    }
}
