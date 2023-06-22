package christine.moldovan.recycle;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class MyPairAdapter extends ArrayAdapter<Pair<String, String>> {
    private int resourceId;

    public MyPairAdapter(Context context, int resourceId, List<Pair<String, String>> pairs) {
        super(context, resourceId, pairs);
        this.resourceId = resourceId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Pair<String, String> pair = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
        }

        TextView titleTextView = convertView.findViewById(R.id.titleTextView);
        TextView snippetTextView = convertView.findViewById(R.id.snippetTextView);

        titleTextView.setText(pair.first);
        snippetTextView.setText(pair.second);

        return convertView;
    }
}
