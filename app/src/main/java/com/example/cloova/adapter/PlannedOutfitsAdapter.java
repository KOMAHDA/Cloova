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
import com.example.cloova.model.PlannedOutfit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlannedOutfitsAdapter extends RecyclerView.Adapter<PlannedOutfitsAdapter.PlannedOutfitViewHolder> {

    private List<PlannedOutfit> plannedOutfits;
    private Context context;
    private OnItemDeleteListener deleteListener;

    private static final String TAG = "PlannedOutfitsAdapter";

    public interface OnItemDeleteListener {
        void onDeleteClick(PlannedOutfit outfit);
    }

    public PlannedOutfitsAdapter(Context context, List<PlannedOutfit> plannedOutfits, OnItemDeleteListener deleteListener) {
        this.context = context;
        this.plannedOutfits = plannedOutfits;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<PlannedOutfit> newOutfits) {
        this.plannedOutfits.clear();
        if (newOutfits != null) {
            this.plannedOutfits.addAll(newOutfits);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlannedOutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_planned_outfit, parent, false);
        return new PlannedOutfitViewHolder(view, deleteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull PlannedOutfitViewHolder holder, int position) {
        PlannedOutfit outfit = plannedOutfits.get(position);
        holder.bind(outfit, context);
    }

    @Override
    public int getItemCount() {
        return plannedOutfits == null ? 0 : plannedOutfits.size();
    }


    static class PlannedOutfitViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlannedDate, tvPlannedWeatherInfo, tvPlannedOutfitDetails;
        ImageButton btnDeletePlannedOutfit;
        ImageView ivPlannedMannequin;
        ImageView ivPlannedOutfitOuterwear, ivPlannedOutfitTop, ivPlannedOutfitBottom, ivPlannedOutfitShoes;

        OnItemDeleteListener deleteListener;

        public PlannedOutfitViewHolder(@NonNull View itemView, OnItemDeleteListener deleteListener) {
            super(itemView);
            this.deleteListener = deleteListener;
            tvPlannedDate = itemView.findViewById(R.id.tv_planned_date);
            tvPlannedWeatherInfo = itemView.findViewById(R.id.tv_planned_weather_info);
            tvPlannedOutfitDetails = itemView.findViewById(R.id.tv_planned_outfit_details);
            btnDeletePlannedOutfit = itemView.findViewById(R.id.btn_delete_planned_outfit);
            ivPlannedMannequin = itemView.findViewById(R.id.iv_planned_mannequin);

            ivPlannedOutfitOuterwear = itemView.findViewById(R.id.iv_planned_outfit_outerwear);
            ivPlannedOutfitTop = itemView.findViewById(R.id.iv_planned_outfit_top);
            ivPlannedOutfitBottom = itemView.findViewById(R.id.iv_planned_outfit_bottom);
            ivPlannedOutfitShoes = itemView.findViewById(R.id.iv_planned_outfit_shoes);

            btnDeletePlannedOutfit.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onDeleteClick((PlannedOutfit) itemView.getTag());
                }
            });
        }

        void bind(final PlannedOutfit outfit, final Context context) {
            itemView.setTag(outfit);

            tvPlannedDate.setText(formatDate(outfit.getPlanDate()));
            tvPlannedWeatherInfo.setText(String.format(Locale.getDefault(), "%s°C, %s, %s стиль",
                    (int) outfit.getTemperature(), outfit.getWeatherDescription(), outfit.getStyleName()));

            ivPlannedOutfitOuterwear.setVisibility(View.GONE);
            ivPlannedOutfitTop.setVisibility(View.GONE);
            ivPlannedOutfitBottom.setVisibility(View.GONE);
            ivPlannedOutfitShoes.setVisibility(View.GONE);

            StringBuilder outfitTextBuilder = new StringBuilder();
            Map<String, ClothingItem> outfitItemsMap = outfit.getOutfitItems();

            if (outfitItemsMap.containsKey("верхняя одежда")) {
                ClothingItem item = outfitItemsMap.get("верхняя одежда");
                setOutfitImage(ivPlannedOutfitOuterwear, item.getImageResourceName(), context);
                outfitTextBuilder.append("🧥 ").append(item.getName()).append("\n");
            }
            if (outfitItemsMap.containsKey("верх")) {
                ClothingItem item = outfitItemsMap.get("верх");
                setOutfitImage(ivPlannedOutfitTop, item.getImageResourceName(), context);
                outfitTextBuilder.append("👕 ").append(item.getName()).append("\n");
            }
            if (outfitItemsMap.containsKey("платья/юбки")) {
                ClothingItem item = outfitItemsMap.get("платья/юбки");
                setOutfitImage(ivPlannedOutfitTop, item.getImageResourceName(), context);
                outfitTextBuilder.append("👗 ").append(item.getName()).append("\n");
            }
            if (outfitItemsMap.containsKey("низ")) {
                ClothingItem item = outfitItemsMap.get("низ");
                setOutfitImage(ivPlannedOutfitBottom, item.getImageResourceName(), context);
                outfitTextBuilder.append("👖 ").append(item.getName()).append("\n");
            }
            if (outfitItemsMap.containsKey("обувь")) {
                ClothingItem item = outfitItemsMap.get("обувь");
                setOutfitImage(ivPlannedOutfitShoes, item.getImageResourceName(), context);
                outfitTextBuilder.append("👟 ").append(item.getName()).append("\n");
            }

            tvPlannedOutfitDetails.setText(outfitTextBuilder.toString().trim());
        }

        private String formatDate(String dateStr) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", new Locale("ru", "RU"));
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date: " + dateStr, e);
                return dateStr;
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