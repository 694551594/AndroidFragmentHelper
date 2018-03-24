package cn.yhq.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment管理帮助类，提供Fragment的添加、切换与状态保存和恢复
 *
 * @author Yanghuiqiang 2015-10-13
 */
public final class FragmentHelper {
    private final static String CurrentFragment = "cn.yhq.fragment.CurrentFragment";
    private Context mContext;
    private List<FragmentInfo> mFragmentInfos = new ArrayList<>();
    private FragmentManager mFragmentManager;
    private int mContainerId;
    private FragmentInfo mLastFragment;
    private OnFragmentChangeListener mOnFragmentChangeListener;

    public void setOnFragmentChangeListener(OnFragmentChangeListener onFragmentChangeListener) {
        this.mOnFragmentChangeListener = onFragmentChangeListener;
    }

    public interface OnFragmentChangeListener {
        void onFragmentChanged(FragmentInfo fragmentInfo);
    }

    public static final class FragmentInfo {
        private final String tag;
        private final Class<? extends Fragment> clss;
        private final Bundle args;
        private Fragment fragment;

        FragmentInfo(String _tag, Class<? extends Fragment> _class, Bundle _args) {
            tag = _tag;
            clss = _class;
            args = _args;
        }

        /**
         * @return the fragment
         */
        public Fragment getFragment() {
            return fragment;
        }

        /**
         * @param fragment the fragment to set
         */
        public void setFragment(Fragment fragment) {
            this.fragment = fragment;
        }

        /**
         * @return the tag
         */
        public String getTag() {
            return tag;
        }

        /**
         * @return the clss
         */
        public Class<? extends Fragment> getClss() {
            return clss;
        }

        /**
         * @return the args
         */
        public Bundle getArgs() {
            return args;
        }

    }

    private FragmentHelper(Context context, FragmentManager fragmentManager, int containerId) {
        this.mContext = context;
        this.mFragmentManager = fragmentManager;
        this.mContainerId = containerId;
    }

    public static FragmentHelper setup(FragmentActivity activity, int containerId) {
        return new FragmentHelper(activity, activity.getSupportFragmentManager(), containerId);
    }

    public static FragmentHelper setup(Fragment fragment, int containerId) {
        return new FragmentHelper(fragment.getContext(), fragment.getChildFragmentManager(), containerId);
    }

    public void saveInstanceState(Bundle bundle) {
        if (this.mLastFragment != null) {
            bundle.putString(CurrentFragment, this.mLastFragment.tag);
        }
    }

    public void restoreInstanceState(Bundle bundle) {
        String curTab = bundle.getString(CurrentFragment);
        if (TextUtils.isEmpty(curTab)) {
            return;
        }
        changeFragment(curTab);
    }

    public void changeFragment(Class<? extends Fragment> fragmentClass) {
        changeFragment(fragmentClass.getName());
    }

    public <T extends Fragment> T getFragment(Class<T> clss) {
        return getFragment(clss.getName());
    }

    public int indexOf(Class<? extends Fragment> clss) {
        for (int i = 0; i < mFragmentInfos.size(); i++) {
            FragmentInfo tab = mFragmentInfos.get(i);
            if (tab.clss == clss) return i;
        }
        return 0;
    }

    public FragmentInfo getLastFragmentInfo() {
        return this.mLastFragment;
    }

    public int getFragmentContainerViewId() {
        return this.mContainerId;
    }

    public <T extends Fragment> T getFragment(String tag) {
        for (int i = 0; i < mFragmentInfos.size(); i++) {
            FragmentInfo tab = mFragmentInfos.get(i);
            if (tab.tag.equals(tag)) return (T) tab.fragment;
        }
        return null;
    }

    public void changeFragment(int position) {
        Class<? extends Fragment> fragmentClass = this.mFragmentInfos.get(position).getClss();
        this.changeFragment(fragmentClass);
    }

    public void changeFragment(String tag) {
        FragmentTransaction ft = doChanged(tag, null);
        if (ft != null) ft.commit();
        if (mOnFragmentChangeListener != null) {
            FragmentInfo fragmentInfo = null;
            for (int i = 0; i < mFragmentInfos.size(); i++) {
                FragmentInfo tab = mFragmentInfos.get(i);
                if (tab.tag.equals(tag)) fragmentInfo = tab;
            }
            mOnFragmentChangeListener.onFragmentChanged(fragmentInfo);
        }
    }

    private FragmentTransaction doChanged(String tag, FragmentTransaction ft) {
        FragmentInfo newFragmentInfo = null;
        for (int i = 0; i < mFragmentInfos.size(); i++) {
            FragmentInfo tab = mFragmentInfos.get(i);
            if (tab.tag.equals(tag)) newFragmentInfo = tab;
        }

        if (newFragmentInfo == null) throw new IllegalStateException(
                (new StringBuilder()).append("No tab known for tag ").append(tag).toString());
        if (mLastFragment != newFragmentInfo) {
            if (ft == null) ft = mFragmentManager.beginTransaction();
            if (mLastFragment != null && mLastFragment.fragment != null)
                ft.hide(mLastFragment.fragment);
            if (newFragmentInfo != null) {
                if (newFragmentInfo.fragment == null) {
                    newFragmentInfo.fragment = mFragmentManager.findFragmentByTag(tag);
                    if (newFragmentInfo.fragment == null) {
                        newFragmentInfo.fragment = Fragment.instantiate(mContext, newFragmentInfo.clss.getName(), newFragmentInfo.args);
                        ft.add(mContainerId, newFragmentInfo.fragment, newFragmentInfo.tag).hide(newFragmentInfo.fragment);
                    }
                } else {
                    ft.show(newFragmentInfo.fragment);
                    mFragmentManager.executePendingTransactions();
                }
            }
            mLastFragment = newFragmentInfo;
        }
        return ft;
    }

    private <T extends Fragment> T add(String tag, Class<T> clss, Bundle args) {
        if (mContainerId == 0) {
            throw new IllegalArgumentException(
                    "Fragment容器mContainerId不可为空");
        }
        FragmentInfo info = new FragmentInfo(tag, clss, args);
        info.fragment = mFragmentManager.findFragmentByTag(tag);
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        if (info.fragment == null || !info.fragment.isAdded()) {
            info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            ft.add(mContainerId, info.fragment, tag).hide(info.fragment);
            ft.commit();
        } else {
            ft.hide(info.fragment);
            ft.commit();
        }
        mFragmentInfos.add(info);
        return (T) info.fragment;
    }

    public <T extends Fragment> T addFragment(Class<T> fragment, Bundle args) {
        String tag = fragment.getName();
        return add(tag, fragment, args);
    }

    public <T extends Fragment> T addFragment(Class<T> fragment) {
        return addFragment(fragment, null);
    }

}
