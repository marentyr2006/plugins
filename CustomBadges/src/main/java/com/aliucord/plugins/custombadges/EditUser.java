package com.aliucord.plugins.custombadges;

import android.annotation.SuppressLint;
import android.view.*;
import android.widget.*;
import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.res.ResourcesCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.*;

import com.aliucord.plugins.CustomBadges;
import com.aliucord.Constants;
import com.aliucord.Utils;
import com.aliucord.PluginManager;
import com.aliucord.api.SettingsAPI;
import com.aliucord.api.NotificationsAPI;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Divider;
import com.aliucord.views.Button;
import com.aliucord.entities.NotificationData;

import com.discord.views.CheckedSetting;
import com.discord.views.RadioManager;
import com.discord.widgets.user.profile.UserProfileHeaderView;
import com.discord.stores.*;
import com.discord.models.user.User;
import com.lytefast.flexinput.R;

import kotlin.Unit;
import java.util.*;

@SuppressLint("SetTextI18n")
public final class EditUser extends SettingsPage {
    private User user;
    private final SettingsAPI settings;
    public EditUser(SettingsAPI settings, Long userId) {
        this.user = StoreStream.getUsers().getUsers().get(userId);
        this.settings = settings;
    }

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void onViewBound(View view) {
        super.onViewBound(view);
        var context = view.getContext();
        if(user == null) {
            Utils.showToast(context, "User invalid or not found");
            getActivity().onBackPressed();
        } else {
            setActionBarTitle("Edit Badges");
            setActionBarSubtitle(user.getUsername());
            setPadding(0);

            var layout = getLinearLayout();
            ProfileWidget profile = new ProfileWidget(context, user);
            layout.addView(profile);
            
            Button addBadge = new Button(context);
            addBadge.setText("Add Badge");
            addBadge.setOnClickListener(v -> {
                new SettingsSheet(this, settings, user.getId()).show(getParentFragmentManager(), "ADD_BADGE");
            });
            
            LinearLayout buttons = new LinearLayout(context);
            buttons.setOrientation(LinearLayout.VERTICAL);
            int p = Utils.dpToPx(16);
            buttons.setPadding(p,p,p,0);
            buttons.addView(addBadge);
            layout.addView(buttons);

            Map<Long, List> userBadges = settings.getObject("userBadges", new HashMap<>(), CustomBadges.badgeStoreType);
            List<StoredBadge> badgeList = new ArrayList<>();
            if (userBadges.containsKey(user.getId())) {
                var customBadges = userBadges.get(user.getId());
                List<StoredBadge> cBadgeList = new ArrayList<>();
                for (var storedBadge : customBadges) {
                    StoredBadge badge = StoredBadge.copy(storedBadge);
                    cBadgeList.add(badge);
                }
                badgeList = cBadgeList;
            }

            RecyclerView badgesView = new RecyclerView(context);
            badgesView.setLayoutManager(new LinearLayoutManager(context));
            badgesView.setAdapter(new EditableBadgeAdapter(this, badgeList, user.getId()));
            badgesView.setPadding(p,0,p,p);
            layout.addView(badgesView);
        }
        
    }

    private CheckedSetting createSwitch(Context context, SettingsAPI sets, String key, String label, String subtext, boolean defaultValue) {
        CheckedSetting cs = Utils.createCheckedSetting(context, CheckedSetting.ViewType.SWITCH, label, subtext);
        cs.setChecked(sets.getBool(key, defaultValue));
        cs.setOnCheckedListener(c -> sets.setBool(key, c));
        return cs;
    }
}