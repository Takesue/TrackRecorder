package jp.takes.apps.recordtracker;

import java.util.ArrayList;

public class SlideViewGroup {

	private ArrayList<SlideView> views = new ArrayList<SlideView>();
	private boolean exclusion = true;

	public SlideViewGroup() {
	}

	public SlideViewGroup(boolean exclusion) {
		this.exclusion = exclusion;
	}

	public void add(SlideView v) {
		views.add(v);
	}

	public void select(int index) {
		if (index >= views.size()) {
			return;
		}
		if (exclusion) {
			for (int i = 0; i < views.size(); i++) {
				SlideView v = views.get(i);
				if ((i != index && v.isOpened()) || (i == index)) {
					v.toggle();
				}
			}
		} else {
			SlideView v = views.get(index);
			v.toggle();
		}
	}	
	
}
