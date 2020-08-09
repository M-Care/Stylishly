package dynasty.software.the.stylishly.ui.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Author : Aduraline.
 */

public class BaseViewHolder extends RecyclerView.ViewHolder {

    /*
    * ButterKnife enabled viewholder
    * */
    public BaseViewHolder(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }
}
