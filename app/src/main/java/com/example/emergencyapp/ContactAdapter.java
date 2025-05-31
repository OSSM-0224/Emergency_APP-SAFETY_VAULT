package com.example.emergencyapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnItemClickListener {
        void onCallClick(int position);
        void onDeleteClick(int position);
    }

    private List<ContactModel> contactList;
    private OnItemClickListener listener;

    public ContactAdapter(List<ContactModel> contactList, OnItemClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.numbers_entry, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactModel contact = contactList.get(position);
        holder.nameTv.setText(contact.getName());
        holder.phoneTv.setText(contact.getPhone());
        holder.bloodTv.setText(contact.getBloodGroup());

        holder.callBtn.setOnClickListener(v -> {
            if (listener != null) listener.onCallClick(position);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, phoneTv, bloodTv;
        Button callBtn, deleteBtn;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameOfPerson);
            phoneTv = itemView.findViewById(R.id.phnNo);
            bloodTv = itemView.findViewById(R.id.bloodGrp);
            callBtn = itemView.findViewById(R.id.callBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}

