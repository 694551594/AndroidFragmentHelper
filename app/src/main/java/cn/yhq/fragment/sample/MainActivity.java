package cn.yhq.fragment.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import cn.yhq.fragment.FragmentHelper;

public class MainActivity extends AppCompatActivity {

    private FragmentHelper fragmentHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentHelper = FragmentHelper.setup(this, R.id.fragment_container);
        fragmentHelper.addFragment(Fragment1.class);
        fragmentHelper.addFragment(Fragment2.class);

        fragmentHelper.changeFragment(Fragment1.class);

        RadioGroup radioGroup = (RadioGroup) this.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_button1:
                        fragmentHelper.changeFragment(Fragment1.class);
                        break;
                    case R.id.radio_button2:
                        fragmentHelper.changeFragment(Fragment2.class);
                        break;
                }
            }
        });
    }


}
