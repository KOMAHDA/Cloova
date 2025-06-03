package com.example.cloova.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cloova.DatabaseHelper;
import com.example.cloova.R;
import com.example.cloova.model.ClothingItem;
import com.example.cloova.model.SavedOutfit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SavedOutfitsAdapter extends RecyclerView.Adapter<SavedOutfitsAdapter.SavedOutfitViewHolder> {

    private List<SavedOutfit> savedOutfits;
    private Context context;
    private OnItemDeleteListener deleteListener;

    private static final String TAG = "SavedOutfitsAdapter";

    public interface OnItemDeleteListener {
        void onDeleteClick(SavedOutfit outfit);
    }

    public SavedOutfitsAdapter(Context context, List<SavedOutfit> savedOutfits, OnItemDeleteListener deleteListener) {
        this.context = context;
        this.savedOutfits = savedOutfits;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<SavedOutfit> newOutfits) {
        this.savedOutfits.clear();
        if (newOutfits != null) {
            this.savedOutfits.addAll(newOutfits);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavedOutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_outfit, parent, false);
        return new SavedOutfitViewHolder(view, deleteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedOutfitViewHolder holder, int position) {
        SavedOutfit outfit = savedOutfits.get(position);
        holder.bind(outfit, context);
    }

    @Override
    public int getItemCount() {
        return savedOutfits == null ? 0 : savedOutfits.size();
    }


    static class SavedOutfitViewHolder extends RecyclerView.ViewHolder {
        TextView tvSavedDate, tvSavedWeatherInfo, tvSavedOutfitDetails;
        ImageButton btnDeleteOutfit;
        ImageView ivSavedMannequin;
        ImageView ivSavedOutfitOuterwear, ivSavedOutfitTop, ivSavedOutfitBottom, ivSavedOutfitShoes;

        OnItemDeleteListener deleteListener;

        public SavedOutfitViewHolder(@NonNull View itemView, OnItemDeleteListener deleteListener) {
            super(itemView);
            this.deleteListener = deleteListener;
            tvSavedDate = itemView.findViewById(R.id.tv_saved_date);
            tvSavedWeatherInfo = itemView.findViewById(R.id.tv_saved_weather_info);
            tvSavedOutfitDetails = itemView.findViewById(R.id.tv_saved_outfit_details);
            btnDeleteOutfit = itemView.findViewById(R.id.btn_delete_outfit);
            ivSavedMannequin = itemView.findViewById(R.id.iv_saved_mannequin);

            ivSavedOutfitOuterwear = itemView.findViewById(R.id.iv_saved_outfit_outerwear);
            ivSavedOutfitTop = itemView.findViewById(R.id.iv_saved_outfit_top);
            ivSavedOutfitBottom = itemView.findViewById(R.id.iv_saved_outfit_bottom);
            ivSavedOutfitShoes = itemView.findViewById(R.id.iv_saved_outfit_shoes);
            btnDeleteOutfit.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onDeleteClick((SavedOutfit) itemView.getTag());
                }
            });
        }

        void bind(final SavedOutfit outfit, final Context context) {
            itemView.setTag(outfit);

            tvSavedDate.setText(formatDate(outfit.getDateSaved()));
            tvSavedWeatherInfo.setText(String.format(Locale.getDefault(), "%s°C, %s, %s стиль",
                    (int) outfit.getTemperature(), outfit.getWeatherDescription(), outfit.getStyleName()));

            ivSavedOutfitOuterwear.setVisibility(View.GONE);
            ivSavedOutfitTop.setVisibility(View.GONE);
            ivSavedOutfitBottom.setVisibility(View.GONE);
            ivSavedOutfitShoes.setVisibility(View.GONE);

            StringBuilder outfitTextBuilder = new StringBuilder();
            Map<String, ClothingItem> outfitItemsMap = outfit.getOutfitItems();

            if (outfitItemsMap.containsKey("верхняя одежда")) {
                ClothingItem item = outfitItemsMap.get("верхняя одежда");
                setOutfitImage(ivSavedOutfitOuterwear, item.getImageResourceName(), context);
                outfitTextBuilder.append("🧥 ").append(item.getName()).append("\n");
            }
            if (outfitItemsMap.containsKey("верх")) {
                ClothingItem item = outfitItemsMap.get("верх");
                setOutfitImage(ivSavedOutfitTop, item.getImageResourceName(), context);
                outfitTextBuilder.append("👕 ").append(item.getName()).append("\n");
            }
            if (outfitItemsMap.containsKey("платья/юбки")) {
                ClothingItem item = outfitItemsMap.get("платья/юбки");
                setOutfitImage(ivSavedOutfitTop, item.getImageResourceName(), context);
                outfitTextBuilder.append("👗 ").append(item.getName()).append("\n");
            }
            if (outfitItemsMap.containsKey("низ")) {
                ClothingItem item = outfitItemsMap.get("низ");
                setOutfitImage(ivSavedOutfitBottom, item.getImageResourceName(), context);
                outfitTextBuilder.append("👖 ").append(item.getName()).append("\n");
            }
            if (outfitItemsMap.containsKey("обувь")) {
                ClothingItem item = outfitItemsMap.get("обувь");
                setOutfitImage(ivSavedOutfitShoes, item.getImageResourceName(), context);
                outfitTextBuilder.append("👟 ").append(item.getName()).append("\n");
            }

            tvSavedOutfitDetails.setText(outfitTextBuilder.toString().trim());
        }

        private String formatDate(String dateTimeStr) {
            try {

                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy, HH:mm", new Locale("ru", "RU"));
                Date date = inputFormat.parse(dateTimeStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + dateTimeStr, e);
                return dateTimeStr;
            }
        }

        private void setOutfitImage(ImageView imageView, String imageResourceName, Context context) {
            if (imageView == null || imageResourceName == null || imageResourceName.isEmpty()) {
                Log.w(TAG, "setOutfitImage: ImageView is null or imageResourceName is empty.");
                return;
            }

            int resId = context.getResources().getIdentifier(imageResourceName, "drawable", context.getPackageName());
            if (resId != 0) {
                imageView.setImageResource(resId);
                imageView.setVisibility(View.VISIBLE);
                Log.d(TAG, "setOutfitImage: Set '" + imageResourceName + "' to " + imageView.getId());
            } else {
                Log.e(TAG, "setOutfitImage: Resource not found for name: " + imageResourceName + ". Setting visibility to GONE.");
                imageView.setVisibility(View.GONE);
            }
        }
    }
}