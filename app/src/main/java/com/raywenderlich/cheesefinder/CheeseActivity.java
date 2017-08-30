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

import android.view.View;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;

public class CheeseActivity extends BaseSearchActivity {
    private ObservableEmitter<String> emitter;
    private Observable<String> searchTextObservable;

    public ObservableEmitter<String> getEmitter() {
        return emitter;
    }

    public void setEmitter(ObservableEmitter<String> emitter) {
        this.emitter = emitter;
    }

    private ObservableOnSubscribe<String> observableOnSubscribe = new ObservableOnSubscribe<String>() {

        @Override
        public void subscribe(ObservableEmitter<String> emitter) throws Exception {
            setEmitter(emitter);
            mSearchButton.setOnClickListener(mSearchButtonOnClickListener);
            emitter.setCancellable(cancellable);
        }
    };
    private Cancellable cancellable = new Cancellable() {
        @Override
        public void cancel() throws Exception {
            mSearchButton.setOnClickListener(null);
        }
    };

    private Observable<String> createButtonClickObservable() {
        return Observable.create(observableOnSubscribe);
    }

    private View.OnClickListener mSearchButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            getEmitter().onNext(mQueryEditText.getText().toString());
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        searchTextObservable = createButtonClickObservable();

    }
}
