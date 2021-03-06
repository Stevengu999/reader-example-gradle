package com.payneteasy.example;

import android.app.Activity;
import android.widget.TextView;
import com.payneteasy.android.sdk.card.BankCard;
import com.payneteasy.android.sdk.processing.ConfigurationContinuation;
import com.payneteasy.android.sdk.processing.IProcessingStageListener;
import com.payneteasy.android.sdk.processing.ProcessingContinuation;
import com.payneteasy.android.sdk.processing.ProcessingStageEvent;
import com.payneteasy.android.sdk.reader.*;
import com.payneteasy.reader.i18n.IReaderI18nService;
import com.payneteasy.reader.i18n.ReaderI18nServiceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class SimpleCardReaderPresenter implements ICardReaderPresenter {

    private final static Logger LOG = LoggerFactory.getLogger(SimpleCardReaderPresenter.class);

    private final TextView statusView;
    private final Activity activity;
    private final IReaderI18nService translationService;
    private final Locale defaultLocale = new Locale("ru");

    public SimpleCardReaderPresenter(Activity aActivity, TextView aView) {
        statusView = aView;
        activity = aActivity;
        try {
            translationService = new ReaderI18nServiceBuilder()
                    .addPropertyFile(Locale.ENGLISH, "reader_en.properties")
                    .addPropertyFile(new Locale("ru"), "reader_ru.properties")
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException("Error creating i18n service", e);
        }
    }


    @Override
    public ProcessingContinuation onCard(BankCard bankCard) {

        setStatus("onCard: %s", bankCard);

        return ProcessingContinuation.Builder
                .startSaleOnline()
                .processingBaseUrl  ( Config.SERVER_BASE_URL)
                .merchantLogin      ( Config.MERCHANT_LOGIN )
                .merchantControlKey ( Config.MERCHANT_KEY   )
                .merchantEndPointId ( Config.END_POINT_ID   )
                .orderDescription   ( "test description"    )
                .orderInvoiceNumber ( "invoice-"+System.currentTimeMillis())
                .orderMerchantData  ( "custom merchant data for internal use")
                .customerPhone      ( "+7 (499) 918-64-41"  )
                .customerEmail      ( "info@payneteasy.com" )
                .customerCountry    ( "RUS"                 )
                .listener(new IProcessingStageListener() {
                    @Override
                    public void onStageChanged(ProcessingStageEvent aEvent) {
                        String message = translationService.translateProcessingEvent(defaultLocale, aEvent);
                        setStatus(message);
                    }
                })
                .build();

    }

    private void setStatus(final String aFormat, final Object ... args) {
        final String outputText = String.format(aFormat, args);
        LOG.debug("Status: {}", outputText);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setText(outputText);
            }
        });
    }

    @Override
    public void onReaderSerialNumber(String aKsn) {
        setStatus("onReaderSerialNumber: %s", aKsn);
    }

    @Override
    public void cardReaderStateChanged(CardReaderEvent cardReaderEvent) {
        setStatus("cardReaderStateChanged: %s", translationService.translateReaderEvent(defaultLocale, cardReaderEvent));
    }

    @Override
    public void onCardError(CardError cardError) {
        setStatus("onCardError: %s", translationService.translateCardError(defaultLocale, cardError));
    }

    @Override
    public void onReaderNotSupported(CardReaderProblem aProblem) {
        setStatus("onReaderNotSupported: %s", translationService.translateCardReaderProblem(defaultLocale, aProblem));
    }

    @Override
    public void onAudioData(short[] shorts, int i) {
        // for visualization
    }

    @Override
    public ConfigurationContinuation onConfiguration() {
        return new ConfigurationContinuation.Builder()
                .configDir              ( new File(activity.getFilesDir(), "miura-config"))
                .configurationBaseUrl   ( Config.SERVER_CONFIG_URL  )
                .merchantLogin          ( Config.MERCHANT_LOGIN     )
                .merchantControlKey     ( Config.MERCHANT_KEY       )
                .merchantEndPointId     ( Config.END_POINT_ID       )
                .build();
    }
}
