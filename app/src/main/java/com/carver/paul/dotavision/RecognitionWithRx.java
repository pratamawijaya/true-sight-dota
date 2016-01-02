package com.carver.paul.dotavision;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.support.v4.util.Pair;
import android.util.Log;

import com.carver.paul.dotavision.ImageRecognition.HeroFromPhoto;
import com.carver.paul.dotavision.ImageRecognition.Recognition;
import com.carver.paul.dotavision.ImageRecognition.SimilarityTest;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;

/**
 * This class does all the work in the UI thread showing that the image is being processed,
 * processes the image in a background thread to identify the five heroes, and then presents the
 * results in the UI
 */
class RecognitionWithRx {
    static final String TAG = "RecognitionRx";

    //TODO-someday: improve performance by saving mSimilarityTest mHeroInfoList once they have
    // been loaded the first time
//    private SimilarityTest mSimilarityTest;
    private AsyncSubject<List<HeroInfo>> mXmlInfoRx;
    private AsyncSubject<SimilarityTest> mSimilarityTestRx;
    private Subscriber<HeroFromPhoto> mHeroRecognitionSubscriberRx;
    private Subscriber<Pair<SimilarityTest, Bitmap>> mLoadingCompleteRx;

    /**
     * Reads the XML and opens the hero images in the background. This should be launched when the
     * app is first launched, and then everything should be ready when it is needed.
     */
    RecognitionWithRx(final Context context) {
        StartXmlLoading(context);
        StartSimilarityTestLoading(context);
    }

    public List<HeroInfo> getXmlInfo() {
        return mXmlInfoRx.getValue();
    }

    //TODO: Look in to the "Suspending all threads took" messages I'm getting

    /**
     *Create an observable to load the xml file in the background. mXmlInfoRx is subscribed to
     * it and will complete when the file has been loaded.
     * @param context
     */
    private void StartXmlLoading(final Context context) {
        mXmlInfoRx = AsyncSubject.create();

        Observable.create(new Observable.OnSubscribe<List<HeroInfo>>() {
            @Override
            public void call(Subscriber<? super List<HeroInfo>> observer) {
                XmlResourceParser parser = context.getResources().getXml(R.xml.hero_info_from_web);
                List<HeroInfo> heroInfoList = new ArrayList<>();
                LoadHeroXml.Load(parser, heroInfoList);
                observer.onNext(heroInfoList);
                observer.onCompleted();
            }
        })
//TODO-beauty: check that the XML parsing should be done on the io thread. Is all the io work done
// at the start instantly?
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mXmlInfoRx);
    }

    /**
     *Create an observable to Similarity Test (all the hero images used for detection)in the
     * background. mSimilarityTestRx is subscribed to it and will complete when the file has been
     * loaded.
     * @param context
     */
    private void StartSimilarityTestLoading(final Context context) {
        mSimilarityTestRx = AsyncSubject.create();

        Observable.create(new Observable.OnSubscribe<SimilarityTest>() {
            @Override
            public void call(Subscriber<? super SimilarityTest> subscriber) {
                subscriber.onNext(new SimilarityTest(context));
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mSimilarityTestRx);
    }

    public void Run(final MainActivity mainActivity, final Bitmap photoBitmap) {
        resetSubscribers(mainActivity);

        // Show that image processing is happening in the background (pulse the large camera button)
        mainActivity.preRecognitionUiTasks(photoBitmap);

        //TODO: should this be an observable which takes the photo bitmap as input in a more Rx way?

        Observable.zip(mXmlInfoRx, mSimilarityTestRx, new Func2<List<HeroInfo>, SimilarityTest,
                Pair<SimilarityTest, Bitmap>>() {
            @Override
            public Pair<SimilarityTest, Bitmap> call(List<HeroInfo> heroInfoList,
                                            SimilarityTest similarityTest) {
                return new Pair<SimilarityTest, Bitmap>(similarityTest, photoBitmap);
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mLoadingCompleteRx);


/*        // Zipping the two observables used in the loading task will ensure that both the xml and
            // the similarity test have loaded successfully before attempting to do tje image
        // recognition
        Observable.zip(mXmlInfoRx, mSimilarityTestRx, new Func2<List<HeroInfo>, SimilarityTest,
                List<HeroFromPhoto>>() {
            @Override
            public List<HeroFromPhoto> call(List<HeroInfo> heroInfoList,
                                            SimilarityTest similarityTest) {
                return Recognition.Run(photoBitmap, similarityTest);
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mHeroRecognitionSubscriberRx);*/
    }

    /**
     * This needs to be called by MainActivity.onDestory to ensure all subscribers no longer
     * subscribe to any observers, otherwise there could be memory leaks.
     */
    public void onDestroy() {
        ensureAllSubscribersUnsubscribed();
    }

    /**
     * Sets up mHeroRecognitionSubscriberRx so that when onNext is called it will run
     * mainActivity.postRecognitionUiTasks and all the heroes will be visible.
     * @param mainActivity
     */
    private void resetSubscribers(final MainActivity mainActivity) {
        ensureAllSubscribersUnsubscribed();

        mHeroRecognitionSubscriberRx = new Subscriber<HeroFromPhoto>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(HeroFromPhoto hero) {
                if(hero == null) {
                    mainActivity.postRecognitionUiTasks();
                } else {
                    Log.d(TAG, "Adding " + hero.getSimilarityList().get(0).hero.name);
                    mainActivity.addHero(hero);
                }
            }
        };

        // Set up the new subscriber
        mLoadingCompleteRx = new Subscriber<Pair<SimilarityTest, Bitmap>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Pair<SimilarityTest, Bitmap> pair) {
                Recognition.Run(pair.second, pair.first)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(mHeroRecognitionSubscriberRx);
            }
        };
    }

    private void ensureAllSubscribersUnsubscribed() {
        //TODO-beauty: test if we actually need to unsubscribe from the observer, and if
        // unsubscribing like this does clear it from memory
        if(mHeroRecognitionSubscriberRx != null) {
            mHeroRecognitionSubscriberRx.unsubscribe();
            mHeroRecognitionSubscriberRx = null;
        }

        if(mLoadingCompleteRx != null) {
            mLoadingCompleteRx.unsubscribe();
            mLoadingCompleteRx = null;
        }
    }
}
