/*
 * Copyright (c) 2016 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.cheesefinder;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

public class CheeseActivity extends BaseSearchActivity {
    private ObservableEmitter<String> emitter;
    private Observable<String> searchTextObservableButton;
    private Observable<String> textChangeObservable;
    private Observable<String> searchTextObservable;
    private Disposable mDisposable;

    public ObservableEmitter<String> getEmitter() {
        return emitter;
    }

    public void setEmitter(ObservableEmitter<String> emitter) {
        this.emitter = emitter;
    }

    private ObservableOnSubscribe<String> searchButtonObservableOnSubscribe = new ObservableOnSubscribe<String>() {

        @Override
        public void subscribe(ObservableEmitter<String> emitter) throws Exception {
            setEmitter(emitter);
            mSearchButton.setOnClickListener(mSearchButtonOnClickListener);
            emitter.setCancellable(searchButtonObservableCancellable);
        }
    };
    private ObservableOnSubscribe<String> textChangeObservableOnSubscribe = new ObservableOnSubscribe<String>() {

        @Override
        public void subscribe(ObservableEmitter<String> emitter) throws Exception {
            setEmitter(emitter);
            mQueryEditText.addTextChangedListener(watcher);
            emitter.setCancellable(textChangeObservableCancellable);
        }
    };
    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        //4
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            getEmitter().onNext(s.toString());
        }
    };

    private Cancellable searchButtonObservableCancellable = new Cancellable() {
        @Override
        public void cancel() throws Exception {
            mSearchButton.setOnClickListener(null);
        }
    };
    private Cancellable textChangeObservableCancellable = new Cancellable() {
        @Override
        public void cancel() throws Exception {
            mQueryEditText.removeTextChangedListener(watcher);
        }
    };

    private Observable<String> createButtonClickObservable() {
        return Observable.create(searchButtonObservableOnSubscribe);
    }

    private Observable<String> createTextChangeObservable() {
        return Observable
                .create(textChangeObservableOnSubscribe)
                .filter(predicateFilter)
                .debounce(1000, TimeUnit.MILLISECONDS);
    }

    private Predicate<String> predicateFilter = new Predicate<String>() {
        @Override
        public boolean test(String query) throws Exception {
            return query.length() >= 2;
        }
    };
    private View.OnClickListener mSearchButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getEmitter().onNext(mQueryEditText.getText().toString());
        }
    };
    private Consumer<List<String>> consumer = new Consumer<List<String>>() {
        @Override
        public void accept(List<String> query) throws Exception {
            hideProgressBar();
            showResult(query);
        }
    };
    private Consumer<String> viewConsumer = new Consumer<String>() {
        @Override
        public void accept(String a) throws Exception {
            showProgressBar();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        createObservable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    private void createObservable() {
        searchTextObservableButton = createButtonClickObservable();
        textChangeObservable = createTextChangeObservable();
        searchTextObservable = Observable.merge(searchTextObservableButton, textChangeObservable);
        mDisposable = searchTextObservable
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(viewConsumer)
                .observeOn(Schedulers.io())
                .map(mappingFunction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    private Function<String, List<String>> mappingFunction = new Function<String, List<String>>() {
        @Override
        public List<String> apply(String query) throws Exception {
            return mCheeseSearchEngine.search(query);
        }
    };
}
