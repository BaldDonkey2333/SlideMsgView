package com.tulv.slidemsg;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author: tulv
 * @date: 2020/5/25 17:50
 * @description:
 */
public class SlideMsgView extends FrameLayout {
    /**
     * 默认最短消息展示时间
     */
    private final static int DEFAULT_MIN_SHOW_TIME = 1000;
    /**
     * 默认最长消息展示时间
     */
    private final static int DEFAULT_MAX_SHOW_TIME = 2000;
    /**
     * 默认动画执行时长
     */
    private final static int DEFAULT_DURATION_TIME = 500;

    private TextView mTvMsg;

    private Handler mHandler = new Handler();
    /**
     * 横向位移动画距离
     */
    private int mTranslationX;
    /**
     * 任务队列
     */
    private Queue<AnimatorBean> mTaskQueue = new LinkedList<>();
    /**
     * 当前是否正在执行动画
     */
    private boolean isRunning;
    /**
     * 当前执行的动画对象
     */
    private ObjectAnimator mCurrentAnimator;
    /**
     * 最短消息展示时间
     */
    private long mMinShowTime = DEFAULT_MIN_SHOW_TIME;
    /**
     * 最长消息展示时间
     */
    private long mMaxShowTime = DEFAULT_MAX_SHOW_TIME;
    /**
     * 动画执行时长
     */
    private long mDurationTime = DEFAULT_DURATION_TIME;

    private int mBackgroudRes;

    private int mTopPadding;

    private int mBottomPadding;

    private int mLeftPadding;

    private int mRightPadding;

    private int mLeftMargin;

    private int mRightMargin;

    private int mTopMargin;

    private int mBottomMargin;

    private float mTextSize;

    private ColorStateList mTextColor;

    public SlideMsgView(@NonNull Context context) {
        this(context, null);
    }

    public SlideMsgView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMsgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideMsgView);
        mTextColor = typedArray.getColorStateList(R.styleable.SlideMsgView_smv_text_color);
        mTextSize = typedArray.getDimension(R.styleable.SlideMsgView_smv_text_size, 0);
        mLeftMargin = typedArray.getDimensionPixelSize(R.styleable.SlideMsgView_smv_left_margin, 0);
        mRightMargin  = typedArray.getDimensionPixelSize(R.styleable.SlideMsgView_smv_right_margin, 0);
        mTopMargin = typedArray.getDimensionPixelSize(R.styleable.SlideMsgView_smv_top_margin, 0);
        mBottomMargin = typedArray.getDimensionPixelSize(R.styleable.SlideMsgView_smv_bottom_margin, 0);
        mLeftPadding = typedArray.getDimensionPixelSize(R.styleable.SlideMsgView_smv_left_padding, 0);
        mRightPadding = typedArray.getDimensionPixelSize(R.styleable.SlideMsgView_smv_right_padding, 0);
        mTopPadding = typedArray.getDimensionPixelSize(R.styleable.SlideMsgView_smv_top_padding, 0);
        mBottomPadding = typedArray.getDimensionPixelSize(R.styleable.SlideMsgView_smv_bottom_padding, 0);
        mBackgroudRes = typedArray.getResourceId(R.styleable.SlideMsgView_smv_backgroud, 0);
        mMinShowTime = typedArray.getInt(R.styleable.SlideMsgView_smv_min_show_time, DEFAULT_MIN_SHOW_TIME);
        mMaxShowTime = typedArray.getInt(R.styleable.SlideMsgView_smv_max_show_time, DEFAULT_MAX_SHOW_TIME);
        mDurationTime = typedArray.getInt(R.styleable.SlideMsgView_smv_duration_time, DEFAULT_DURATION_TIME);
        typedArray.recycle();

        LayoutInflater.from(getContext()).inflate(R.layout.slide_msg, this, true);
        getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(this);
                        mTranslationX = getWidth(); // 获取宽度
                        return true;
                    }
                });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTvMsg = findViewById(R.id.tv_msg);
        if (mTextColor != null) {
            mTvMsg.setTextColor(mTextColor);
        }
        if (mTextSize > 0) {
            mTvMsg.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }
        updateStyle();
    }

    /**
     * 添加消息到队列
     *
     * @param msg
     */
    public void addMsg(CharSequence msg) {
        if (mTranslationX == 0) {
            mTranslationX = getWidth();
        }
        if (mTranslationX == 0) {
            return;
        }
        final ObjectAnimator transXAnim = ObjectAnimator.ofFloat(mTvMsg, "translationX", mTranslationX, 0.0f);
        transXAnim.setDuration(mDurationTime);
        transXAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mTvMsg.setVisibility(View.VISIBLE);
                isRunning = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isRunning) {
                    return;
                }
                mCurrentAnimator = null;
                mHandler.removeCallbacks(mRunableOut);
                //移入动画执行完毕，判断当前队列是否有下一个任务，有的话取最短展示时间，没有的话取最大展示时间
                long duration = mTaskQueue.isEmpty() ? mMaxShowTime : mMinShowTime;
                mHandler.postDelayed(mRunableOut, duration);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isRunning = false;
                mCurrentAnimator = null;
                mTvMsg.setVisibility(View.GONE);
                mTvMsg.setTranslationX(0);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        //加入队列
        mTaskQueue.offer(new AnimatorBean(msg, transXAnim));
        //当前无动画执行，从队列中取出一个任务执行
        if (!isRunning) {
            next();
        }
    }

    /**
     * 执行下一个任务
     */
    private void next() {
        final AnimatorBean animatorBean = mTaskQueue.poll();
        if (animatorBean != null) {
            mTvMsg.setText(animatorBean.msg);
            ObjectAnimator transXAnim = animatorBean.animator;
            if (transXAnim != null) {
                mCurrentAnimator = transXAnim;
                transXAnim.start();
            }
        }
    }

    /**
     * 延时退出动画任务
     */
    private Runnable mRunableOut = new Runnable() {
        @Override
        public void run() {
            ObjectAnimator transXAnim = ObjectAnimator.ofFloat(mTvMsg, "translationX", 0.0f, -mTranslationX);
            transXAnim.setDuration(mDurationTime);
            transXAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mCurrentAnimator = null;
                    isRunning = false;
                    mTvMsg.setVisibility(View.GONE);
                    next();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isRunning = false;
                    mCurrentAnimator = null;
                    mTvMsg.setVisibility(View.GONE);
                    mTvMsg.setTranslationX(0);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mCurrentAnimator = transXAnim;
            transXAnim.start();
        }
    };

    /**
     * 清除任务
     */
    public void clearMsg() {
        mTaskQueue.clear();
        mHandler.removeCallbacks(mRunableOut);
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
            mCurrentAnimator = null;
        }
        isRunning = false;
        mTvMsg.setVisibility(View.GONE);
        mTvMsg.setTranslationX(0);
    }


    public void setBackgroudRes(int mBackgroudRes) {
        this.mBackgroudRes = mBackgroudRes;
        updateStyle();
    }


    public void setTopPadding(int mTopPadding) {
        this.mTopPadding = mTopPadding;
        updateStyle();
    }


    public void setBottomPadding(int mBottomPadding) {
        this.mBottomPadding = mBottomPadding;
        updateStyle();
    }

    public void setLeftPadding(int mLeftPadding) {
        this.mLeftPadding = mLeftPadding;
        updateStyle();
    }

    public void setRightPadding(int mRightPadding) {
        this.mRightPadding = mRightPadding;
        updateStyle();
    }

    public void setLeftMargin(int mLeftMargin) {
        this.mLeftMargin = mLeftMargin;
        updateStyle();
    }

    public void setBottomMargin(int mBottomMargin) {
        this.mBottomMargin = mBottomMargin;
        updateStyle();
    }

    public void setTopMargin(int mTopMargin) {
        this.mTopMargin = mTopMargin;
        updateStyle();
    }

    public void setRightMargin(int mRightMargin) {
        this.mRightMargin = mRightMargin;
        updateStyle();
    }

    public void setMinShowTime(long mMinShowTime) {
        this.mMinShowTime = mMinShowTime;
    }

    public void setMaxShowTime(long mMaxShowTime) {
        this.mMaxShowTime = mMaxShowTime;
    }

    public void setDurationTime(long mDurationTime) {
        this.mDurationTime = mDurationTime;
    }

    public void setTextColor(int color) {
        if (mTvMsg != null && color > 0) {
            mTvMsg.setTextColor(getContext().getResources().getColor(color));
        }
    }

    public void setTextSize(float textSize) {
        if (mTvMsg != null) {
            mTvMsg.setTextSize(textSize);
        }
    }

    public void setTextSize(int unit, float size) {
        if (mTvMsg != null) {
            mTvMsg.setTextSize(unit, size);
        }
    }

    private void updateStyle() {
        if (mTvMsg != null) {
            if (mBackgroudRes > 0) {
                mTvMsg.setBackgroundResource(mBackgroudRes);
            }
            mTvMsg.setPadding(mLeftPadding, mTopPadding, mRightPadding, mBottomPadding);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTvMsg.getLayoutParams();
            params.leftMargin = mLeftMargin;
            params.topMargin = mTopMargin;
            params.rightMargin = mRightMargin;
            params.bottomMargin = mBottomMargin;
            mTvMsg.setLayoutParams(params);
        }
    }

    public class AnimatorBean {
        private CharSequence msg;
        private ObjectAnimator animator;

        public AnimatorBean(CharSequence msg, ObjectAnimator animator) {
            this.msg = msg;
            this.animator = animator;
        }

        public CharSequence getMsg() {
            return msg;
        }

        public void setMsg(CharSequence msg) {
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
