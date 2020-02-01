package com.example.audiorecorder.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.audiorecorder.R;
import com.example.audiorecorder.model.Record;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.RecordsHolder> {

    List<Record> records;
    RecordClickListener recordClickListener;

    public RecordsAdapter(List<Record> records, RecordClickListener recordClickListener){
        this.records = records;
        this.recordClickListener = recordClickListener;
    }

    public RecordsAdapter(){
    }

    public void addRecord(Record record){
        records.add(record);
        notifyItemInserted(records.size()-1);
    }

    public void setRecords(List<Record> records){
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_layout, parent, false);
        return new RecordsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsHolder holder, int position) {
        holder.bindViews(records.get(position));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class RecordsHolder extends RecyclerView.ViewHolder{

        TextView filename;

        public RecordsHolder(@NonNull View itemView) {
            super(itemView);
            filename = itemView.findViewById(R.id.filename);

        }

        private void bindViews(final Record record){
            filename.setText(record.getName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recordClickListener.onRecordClick(record);
                }
            });
        }
    }
}



