package de.finnik.passvault.gui.onboarding;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.finnik.passvault.R;

public class OnboardingFragment {

    public static int getCount() {
        return fragments.length;
    }

    private static final Fragment[] fragments = new Fragment[] {
            new FirstScreen(),
            new SecondScreen(),
            new ThirdScreen(),
            new FourthScreen(),
            new FifthScreen()
    };

    public static Fragment getInstance(int position) {
        return fragments[position];
    }

    public static class Screen extends Fragment {
        private final int resource;

        public Screen(@LayoutRes int resource) {
            super();
            this.resource = resource;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(resource, container, false);
            TextView textView = view.findViewById(R.id.text_view_welcome);
            if(textView!=null) {
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
            return view;
        }
    }

    public static class FirstScreen extends Screen {

        public FirstScreen() {
            super(R.layout.fragment_first_screen);
        }
    }
    public static class SecondScreen extends Screen {

        public SecondScreen() {
            super(R.layout.fragment_second_screen);
        }
    }
    public static class ThirdScreen extends Screen {

        public ThirdScreen() {
            super(R.layout.fragment_third_screen);
        }
    }
    public static class FourthScreen extends Screen {

        public FourthScreen() {
            super(R.layout.fragment_fourth_screen);
        }
    }
    public static class FifthScreen extends Screen {

        public FifthScreen() {
            super(R.layout.fragment_fifth_screen);
        }
    }
}