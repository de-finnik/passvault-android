package de.finnik.passvault.gui.onboarding;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import de.finnik.passvault.R;

public class OnboardingActivity extends AppCompatActivity {

    private Button btnSkip, btnNext;
    private TextView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        ViewPagerAdapter mAdapter = new ViewPagerAdapter(this);

        ViewPager2 viewPager2 = findViewById(R.id.view_pager_onboarding);
        viewPager2.setAdapter(mAdapter);
        viewPager2.registerOnPageChangeCallback(pageChangeCallback);

        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        btnSkip.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> {
            int current = viewPager2.getCurrentItem();
            if (current + 1 < OnboardingFragment.getCount()) {
                viewPager2.setCurrentItem(++current);
            } else {
                finish();
            }
        });

        addBottomDots();
    }

    private void addBottomDots() {
        dots = new TextView[OnboardingFragment.getCount()];
        LinearLayout layoutDots = findViewById(R.id.layoutDots);
        for (int i = 0; i < OnboardingFragment.getCount(); i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            layoutDots.addView(dots[i]);
        }
    }

    private void setCurrentPage(int currentPage) {
        for (TextView dot : dots) {
            dot.setTextColor(Color.parseColor("#808080"));
        }
        dots[currentPage].setTextColor(currentPage % 2 == 0 ? Color.parseColor("#ffffff") : Color.parseColor("#000000"));
    }

    /*
     * ViewPager page change listener
     */
    ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            setCurrentPage(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == OnboardingFragment.getCount() - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getText(R.string.onboarding_got_it));
                btnSkip.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(R.string.onboarding_next);
                btnSkip.setVisibility(View.VISIBLE);
            }
        }
    };
}