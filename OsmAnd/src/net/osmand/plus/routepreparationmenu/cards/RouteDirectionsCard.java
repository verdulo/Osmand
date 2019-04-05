package net.osmand.plus.routepreparationmenu.cards;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;
import android.support.v7.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.helpers.FontCache;
import net.osmand.plus.routepreparationmenu.RouteDetailsFragment;
import net.osmand.plus.routing.RouteDirectionInfo;
import net.osmand.plus.views.TurnPathHelper;
import net.osmand.plus.widgets.TextViewEx;
import net.osmand.util.Algorithms;

import java.util.List;

public class RouteDirectionsCard extends BaseCard {

	public RouteDirectionsCard(@NonNull MapActivity mapActivity) {
		super(mapActivity);
	}

	@Override
	public int getCardLayoutId() {
		return R.layout.route_directions_card;
	}

	@Override
	protected void updateContent() {
		LinearLayout root = (LinearLayout) view.findViewById(R.id.items);
		root.removeAllViews();
		createRouteDirections(root);
	}

	private void createRouteDirections(LinearLayout cardsContainer) {
		OsmandApplication app = getMyApplication();
		TextViewEx routeDirectionsTitle = new TextViewEx(app);
		routeDirectionsTitle.setTextColor(getMainFontColor());
		routeDirectionsTitle.setTextSize(15);
		routeDirectionsTitle.setGravity(Gravity.CENTER_VERTICAL);
		int padding = AndroidUtils.dpToPx(app, 16);
		routeDirectionsTitle.setPadding(padding, padding, padding, padding);
		routeDirectionsTitle.setText(R.string.step_by_step);
		routeDirectionsTitle.setTypeface(FontCache.getRobotoMedium(app));
		cardsContainer.addView(routeDirectionsTitle);

		List<RouteDirectionInfo> routeDirections = app.getRoutingHelper().getRouteDirections();
		for (int i = 0; i < routeDirections.size(); i++) {
			RouteDirectionInfo routeDirectionInfo = routeDirections.get(i);
			View view = getRouteDirectionView(i, routeDirectionInfo, routeDirections);
			cardsContainer.addView(view);
		}
	}

	private static String getTimeDescription(OsmandApplication app, RouteDirectionInfo model) {
		final int timeInSeconds = model.getExpectedTime();
		return Algorithms.formatDuration(timeInSeconds, app.accessibilityEnabled());
	}

	private View getRouteDirectionView(final int directionInfoIndex, RouteDirectionInfo model, List<RouteDirectionInfo> directionsInfo) {
		MapActivity mapActivity = getMapActivity();
		if (mapActivity == null) {
			return null;
		}
		OsmandApplication app = mapActivity.getMyApplication();
		ContextThemeWrapper context = new ContextThemeWrapper(mapActivity, nightMode ? R.style.OsmandDarkTheme : R.style.OsmandLightTheme);
		View row = LayoutInflater.from(context).inflate(R.layout.route_info_list_item, null);

		TextView label = (TextView) row.findViewById(R.id.description);
		TextView distanceLabel = (TextView) row.findViewById(R.id.distance);
		TextView timeLabel = (TextView) row.findViewById(R.id.time);
		TextView cumulativeDistanceLabel = (TextView) row.findViewById(R.id.cumulative_distance);
		TextView cumulativeTimeLabel = (TextView) row.findViewById(R.id.cumulative_time);
		ImageView icon = (ImageView) row.findViewById(R.id.direction);
		row.findViewById(R.id.divider).setVisibility(directionInfoIndex == directionsInfo.size() - 1 ? View.INVISIBLE : View.VISIBLE);

		TurnPathHelper.RouteDrawable drawable = new TurnPathHelper.RouteDrawable(mapActivity.getResources(), true);
		drawable.setColorFilter(new PorterDuffColorFilter(getActiveColor(), PorterDuff.Mode.SRC_ATOP));
		drawable.setRouteType(model.getTurnType());
		icon.setImageDrawable(drawable);

		label.setText(model.getDescriptionRoutePart());
		if (model.distance > 0) {
			distanceLabel.setText(OsmAndFormatter.getFormattedDistance(model.distance, app));
			timeLabel.setText(getTimeDescription(app, model));
			row.setContentDescription(label.getText() + " " + timeLabel.getText());
		} else {
			if (Algorithms.isEmpty(label.getText().toString())) {
				label.setText(mapActivity.getString((directionInfoIndex != directionsInfo.size() - 1) ? R.string.arrived_at_intermediate_point : R.string.arrived_at_destination));
			}
			distanceLabel.setText("");
			timeLabel.setText("");
			row.setContentDescription("");
		}
		RouteDetailsFragment.CumulativeInfo cumulativeInfo = RouteDetailsFragment.getRouteDirectionCumulativeInfo(directionInfoIndex, directionsInfo);
		cumulativeDistanceLabel.setText(OsmAndFormatter.getFormattedDistance(cumulativeInfo.distance, app));
		cumulativeTimeLabel.setText(Algorithms.formatDuration(cumulativeInfo.time, app.accessibilityEnabled()));
		row.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CardListener listener = getListener();
				if (listener != null) {
					listener.onCardButtonPressed(RouteDirectionsCard.this, directionInfoIndex);
				}
			}
		});
		return row;
	}
}