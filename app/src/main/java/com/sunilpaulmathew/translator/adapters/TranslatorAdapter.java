/*
 * Copyright (C) 2021-2022 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of The Translator, An application to help translate android apps.
 *
 */

package com.sunilpaulmathew.translator.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.sunilpaulmathew.translator.R;
import com.sunilpaulmathew.translator.utils.StringsItem;
import com.sunilpaulmathew.translator.utils.Translator;
import com.sunilpaulmathew.translator.utils.Utils;

import java.io.File;
import java.util.List;

import in.sunilpaulmathew.sCommon.Utils.sExecutor;
import in.sunilpaulmathew.sCommon.Utils.sUtils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on September 28, 2020
 */
public class TranslatorAdapter extends RecyclerView.Adapter<TranslatorAdapter.ViewHolder> {

    private static List<StringsItem> data;

    public TranslatorAdapter(List<StringsItem> data){
        TranslatorAdapter.data = data;
    }

    @NonNull
    @Override
    public TranslatorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_main, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull TranslatorAdapter.ViewHolder holder, int position) {
        if (Translator.getKeyText() != null && Translator.isTextMatched(data.get(position).getDescription())) {
            holder.description.setText(Utils.fromHtml(data.get(position).getDescription().replace(Translator.getKeyText(),
                    "<b><i><font color=\"" + Color.RED + "\">" + Translator.getKeyText() + "</font></i></b>")));
        } else {
            holder.description.setText(data.get(position).getDescription());
        }
        holder.description.setTextColor(sUtils.isDarkTheme(holder.description.getContext()) ?
                Utils.getThemeAccentColor(holder.description.getContext()) : Color.BLACK);
        holder.layoutCard.setOnClickListener(v -> {
            LayoutInflater mLayoutInflater = LayoutInflater.from(v.getContext());
            View editLayout = mLayoutInflater.inflate(R.layout.layout_string_edit, null);
            AppCompatEditText mText = editLayout.findViewById(R.id.text);
            MaterialCardView mReload = editLayout.findViewById(R.id.reload);

            mReload.setOnClickListener(view -> {
                if (mText.getText() != null && !mText.getText().toString().trim().equals(data.get(position).getDescription())) {
                    mText.setText(data.get(position).getDescription());
                    mText.setSelection(mText.getText().length());
                }
            });

            mText.setText(data.get(position).getDescription());
            mText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().trim().contains("\n")) {
                        mText.setText(s.toString().trim().replace("\n", "\\n"));
                        sUtils.snackBar(mText, v.getContext().getString(R.string.line_break_message)).show();
                    }
                    if (s.toString().trim().contains("<") || s.toString().trim().contains(">")) {
                        sUtils.snackBar(mText, v.getContext().getString(R.string.tag_complete_message)).show();
                    }
                }
            });
            mText.requestFocus();

            new MaterialAlertDialogBuilder(v.getContext())
                    .setView(editLayout)
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.yes, (dialog, id) -> {
                                if (mText.getText() == null || mText.getText().toString().trim().isEmpty()
                                        || mText.getText().toString().equals(data.get(position).getDescription())) {
                                    return;
                                }
                                updateString(position, mText.getText().toString(), v.getContext());
                            }
                    ).show();
        });
        holder.layoutCard.setOnLongClickListener(v -> {
            new MaterialAlertDialogBuilder(holder.layoutCard.getContext())
                    .setMessage(holder.description.getContext().getString(R.string.delete_line_question, holder.description.getText()))
                    .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    })
                    .setPositiveButton(R.string.yes, (dialog, id) ->
                            removeString(position, v.getContext())
                    ).show();
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView layoutCard;
        private final MaterialTextView description;

        public ViewHolder(View view) {
            super(view);
            this.description = view.findViewById(R.id.description);
            this.layoutCard = view.findViewById(R.id.recycler_view_card);
        }
    }

    private void removeString(int position, Context context) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                data.remove(position);
                sUtils.create(Translator.getStrings(data), new File(context.getFilesDir(), "strings.xml"));
            }

            @Override
            public void onPostExecute() {
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, data.size());
            }
        }.execute();
    }

    private void updateString(int position, String text, Context context) {
        new sExecutor() {

            @Override
            public void onPreExecute() {
            }

            @Override
            public void doInBackground() {
                data.get(position).setDescription(text);
                sUtils.create(Translator.getStrings(data), new File(context.getFilesDir(), "strings.xml"));
            }

            @Override
            public void onPostExecute() {
                notifyItemChanged(position);
            }
        }.execute();
    }

}