package com.example.slideshow;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author: tulv
 * @date: 2020/5/25 17:50
 * @description:
 */
public class SlideMsgView extends FrameLayout {

    private TextView mTvMsg;

    private Handler handler = new Handler();

    private int translationX;

    private Queue<AnimatorBean> queue = new LinkedList<>();

    private boolean isRunning = false;

    public SlideMsgView(@NonNull Context context) {
        super(context);
        init();
    }

    public SlideMsgView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.slide_msg, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTvMsg = findViewById(R.id.tv_msg);
    }

    private void next() {
        final AnimatorBean animatorBean = queue.poll();
        mTvMsg.setText(animatorBean.msg);
        mTvMsg.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mTvMsg.getViewTreeObserver().removeOnPreDrawListener(this);
                translationX = mTvMsg.getWidth();
                mTvMsg.setVisibility(View.GONE);
                Log.e("translationX", String.valueOf(translationX));

                ObjectAnimator transXAnim = animatorBean.animator;
                if (transXAnim != null) {
                    transXAnim.start();
                }
                return false;
            }
        });
    }

    private Runnable mRunableOut = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator transXAnim = ObjectAnimator.ofFloat(mTvMsg, "translationX", 0.0f, -translationX);
            transXAnim.setDuration(500);
            transXAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isRunning = false;
                    mTvMsg.setVisibility(View.GONE);
                    next();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isRunning = false;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            transXAnim.start();
        }
    };

    public void addMsg(String msg) {
        if (translationX == 0) {
            return;
        }
        ObjectAnimator transXAnim = ObjectAnimator.ofFloat(mTvMsg, "translationX", translationX, 0.0f);
        transXAnim.setDuration(500);
        transXAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mTvMsg.setVisibility(View.VISIBLE);
                isRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                handler.removeCallbacks(mRunableOut);
                int duration = queue.isEmpty() ? 2000 : 1000;
                handler.postDelayed(mRunableOut, duration);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isRunning = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        queue.offer(new AnimatorBean(msg, transXAnim));
        if (!isRunning) {
            next();
        }
        mTvMsg.setText(msg);
        mTvMsg.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mTvMsg.getViewTreeObserver().removeOnPreDrawListener(this);
                translationX = mTvMsg.getWidth();
                mTvMsg.setVisibility(View.GONE);
                Log.e("translationX", String.valueOf(translationX));
                return false;
            }
        });
    }

    public class AnimatorBean {
        private String msg;
        private ObjectAnimator animator;

        public AnimatorBean(String msg, ObjectAnimator animator) {
            this.msg = msg;
            this.animator = animator;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public ObjectAnimator getAnimator() {
            return animator;
        }

        public void setAnimator(ObjectAnimator animator) {
            this.animator = animator;
        }
    }
}
