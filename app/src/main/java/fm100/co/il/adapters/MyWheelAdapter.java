package fm100.co.il.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wx.wheelview.adapter.BaseWheelAdapter;

import fm100.co.il.MainActivity;
import fm100.co.il.R;
import fm100.co.il.fragments.MyAbout;
import fm100.co.il.models.MyObject;

//////////////////////////////////


public class MyWheelAdapter extends BaseWheelAdapter<MyObject> {

    private Context mContext;

    public MyWheelAdapter(Context context) {
        mContext = context;

    }

    @Override
    protected View bindView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list, null);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.item_name);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (mList.get(position).getMyImage() != null) {
            //viewHolder.textView.setText(mList.get(position).getMyString());
            Picasso.with(MainActivity.getMyApplicationContext()).load(mList.get(position).getMyImage()).into(viewHolder.imageView);
        }
        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

}
