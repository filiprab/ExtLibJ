package com.samourai.wallet.cahoots.multi;

import com.samourai.soroban.cahoots.CahootsContext;
import com.samourai.soroban.cahoots.TypeInteraction;
import com.samourai.wallet.bipFormat.BipFormatSupplier;
import com.samourai.wallet.cahoots.AbstractCahootsService;
import com.samourai.wallet.cahoots.CahootsType;
import com.samourai.wallet.cahoots.CahootsUtxo;
import com.samourai.wallet.cahoots.CahootsWallet;
import com.samourai.wallet.cahoots.stonewallx2.STONEWALLx2;
import com.samourai.wallet.cahoots.stonewallx2.Stonewallx2Service;
import com.samourai.wallet.cahoots.stowaway.Stowaway;
import com.samourai.wallet.cahoots.stowaway.StowawayService;
import com.samourai.wallet.util.TxUtil;
import com.samourai.xmanager.client.XManagerClient;
import org.bitcoinj.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MultiCahootsService extends AbstractCahootsService<MultiCahoots, MultiCahootsContext> {
    private static final Logger log = LoggerFactory.getLogger(MultiCahootsService.class);
    private Stonewallx2Service stonewallx2Service;
    private StowawayService stowawayService;
    private XManagerClient xManagerClient;

    public MultiCahootsService(BipFormatSupplier bipFormatSupplier, NetworkParameters params, Stonewallx2Service stonewallx2Service, StowawayService stowawayService, XManagerClient xManagerClient) {
        super(CahootsType.MULTI, bipFormatSupplier, params, TypeInteraction.TX_BROADCAST_MULTI);
        this.stonewallx2Service = stonewallx2Service;
        this.stowawayService = stowawayService;
        this.xManagerClient = xManagerClient;
    }

    @Override
    public MultiCahoots startInitiator(CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext) throws Exception {
        CahootsContext stowawayContext = cahootsContext.getStowawayContext();
        Stowaway stowaway0 = stowawayService.startInitiator(cahootsWallet, stowawayContext);

        CahootsContext stonewallContext = cahootsContext.getStonewallx2Context();
        STONEWALLx2 stonewall0 = stonewallx2Service.startInitiator(cahootsWallet, stonewallContext);

        MultiCahoots multiCahoots0 = new MultiCahoots(params, stowaway0, stonewall0);
        return multiCahoots0;
    }

    @Override
    public MultiCahoots startCollaborator(CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext, MultiCahoots stonewall0) throws Exception {
        MultiCahoots stonewall1 = doMultiCahoots1_Stonewallx21(stonewall0, cahootsWallet, cahootsContext);
        if (log.isDebugEnabled()) {
            log.debug("# MultiCahoots COUNTERPARTY => step="+stonewall1.getStep());
        }
        return stonewall1;
    }

    @Override
    public MultiCahoots reply(CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext, MultiCahoots multiCahoots) throws Exception {
        int step = multiCahoots.getStep();
        if (log.isDebugEnabled()) {
            log.debug("# MultiCahoots <= step="+step);
        }
        MultiCahoots payload;
        switch (step) {
            case 1:
                // sender
                payload = doMultiCahoots2_Stonewallx22(multiCahoots, cahootsWallet, cahootsContext);
                break;
            case 2:
                // counterparty
                payload = doMultiCahoots3_Stonewallx23_Stowaway1(multiCahoots, cahootsWallet, cahootsContext);
                break;
            case 3:
                // sender
                payload = doMultiCahoots4_Stonewallx24_Stowaway2(multiCahoots, cahootsWallet, cahootsContext);
                break;
            case 4:
                // counterparty
                payload = doMultiCahoots5_Stowaway3(multiCahoots, cahootsWallet, cahootsContext);
                break;
            case 5:
                // sender
                payload = doMultiCahoots6_Stowaway4(multiCahoots, cahootsWallet, cahootsContext);
                break;
            default:
                throw new Exception("Unrecognized #Cahoots step");
        }
        if (payload == null) {
            throw new Exception("Cannot compose #Cahoots");
        }
        if (log.isDebugEnabled()) {
            log.debug("# MultiCahoots => step="+payload.getStep());
        }
        return payload;
    }

    //
    // counterparty
    //
    private MultiCahoots doMultiCahoots1_Stonewallx21(MultiCahoots multiCahoots0, CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext) throws Exception {
        CahootsContext stonewallContext = cahootsContext.getStonewallx2Context();
        STONEWALLx2 stonewall1 = stonewallx2Service.doSTONEWALLx2_1_Multi(multiCahoots0.getStonewallx2(), cahootsWallet, stonewallContext, new ArrayList<>(), xManagerClient);

        MultiCahoots multiCahoots1 = new MultiCahoots(multiCahoots0);
        multiCahoots1.setStonewallx2(stonewall1);
        multiCahoots1.setStep(1);
        return multiCahoots1;
    }

    //
    // sender
    //
    private MultiCahoots doMultiCahoots2_Stonewallx22(MultiCahoots multiCahoots1, CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext) throws Exception {
        // continue stonewallx2
        List<String> seenTxs = new ArrayList<String>();
        for (TransactionInput input : multiCahoots1.getStonewallx2().getTransaction().getInputs()) {
            if (!seenTxs.contains(input.getOutpoint().getHash().toString())) {
                seenTxs.add(input.getOutpoint().getHash().toString());
            }
        }
        CahootsContext stonewallContext = cahootsContext.getStonewallx2Context();
        STONEWALLx2 stonewall2 = stonewallx2Service.doSTONEWALLx2_2(multiCahoots1.getStonewallx2(), cahootsWallet, stonewallContext, seenTxs);

        MultiCahoots multiCahoots2 = new MultiCahoots(multiCahoots1);
        multiCahoots2.setStonewallx2(stonewall2);
        multiCahoots2.setStep(2);
        return multiCahoots2;
    }

    //
    // counterparty
    //
    private MultiCahoots doMultiCahoots3_Stonewallx23_Stowaway1(MultiCahoots multiCahoots2, CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext) throws Exception {
        List<String> seenTxs = new ArrayList<>();
        for(TransactionInput input : multiCahoots2.getStonewallx2().getTransaction().getInputs()) {
            seenTxs.add(input.getOutpoint().getHash().toString());
        }

        CahootsContext stonewallContext = cahootsContext.getStonewallx2Context();
        CahootsContext stowawayContext = cahootsContext.getStowawayContext();
        long multiCahootsFee = MultiCahootsContext.computeMultiCahootsFee(stonewallContext.getAmount());
        long stonewallFee = multiCahoots2.getStonewallx2().getFeeAmount() / 2L;
        long totalFee = multiCahootsFee + stonewallFee;
        log.debug("Stonewall fee: " + stonewallFee);
        log.debug("Stowaway amount:: " + stowawayContext.getAmount());
        stowawayContext.setAmount(totalFee);
        Stowaway stowaway0 = multiCahoots2.getStowaway();
        stowaway0.setSpendAmount(totalFee);
        Stowaway stowaway1 = stowawayService.doStowaway1(stowaway0, cahootsWallet, stowawayContext, seenTxs);

        STONEWALLx2 stonewall3 = stonewallx2Service.doStep3(multiCahoots2.getStonewallx2(), cahootsWallet, stonewallContext);

        MultiCahoots multiCahoots3 = new MultiCahoots(multiCahoots2);
        multiCahoots3.setStowaway(stowaway1);
        multiCahoots3.setStonewallx2(stonewall3);
        multiCahoots3.setStep(3);
        return multiCahoots3;
    }

    //
    // sender
    //
    private MultiCahoots doMultiCahoots4_Stonewallx24_Stowaway2(MultiCahoots multiCahoots3, CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext) throws Exception {
        List<String> seenTxs = new ArrayList<>();
        for(TransactionInput input : multiCahoots3.getStonewallx2().getTransaction().getInputs()) {
            seenTxs.add(input.getOutpoint().getHash().toString());
        }
        for(TransactionInput input : multiCahoots3.getStowaway().getTransaction().getInputs()) {
            seenTxs.add(input.getOutpoint().getHash().toString());
        }

        CahootsContext stonewallContext = cahootsContext.getStonewallx2Context();
        STONEWALLx2 stonewall4 = stonewallx2Service.doStep4(multiCahoots3.getStonewallx2(), cahootsWallet, stonewallContext);

        CahootsContext stowawayContext = cahootsContext.getStowawayContext();
        Stowaway stowaway2 = stowawayService.doStowaway2(multiCahoots3.getStowaway(), cahootsWallet, stowawayContext, seenTxs);

        MultiCahoots multiCahoots4 = new MultiCahoots(multiCahoots3);
        multiCahoots4.setStowaway(stowaway2);
        multiCahoots4.setStonewallx2(stonewall4);
        multiCahoots4.setStep(4);
        return multiCahoots4;
    }

    //
    // counterparty
    //
    private MultiCahoots doMultiCahoots5_Stowaway3(MultiCahoots multiCahoots4, CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext) throws Exception {
        // continue stowaway
        CahootsContext stowawayContext = cahootsContext.getStowawayContext();
        Stowaway stowaway3 = stowawayService.doStep3(multiCahoots4.getStowaway(), cahootsWallet, stowawayContext);
        MultiCahoots multiCahoots5 = new MultiCahoots(multiCahoots4);
        multiCahoots5.setStowaway(stowaway3);
        multiCahoots5.setStep(5);
        return multiCahoots5;
    }

    //
    // sender
    //
    private MultiCahoots doMultiCahoots6_Stowaway4(MultiCahoots multiCahoots5, CahootsWallet cahootsWallet, MultiCahootsContext cahootsContext) throws Exception {
        // continue stowaway
        CahootsContext stowawayContext = cahootsContext.getStowawayContext();
        Stowaway stowaway4 = stowawayService.doStep4(multiCahoots5.getStowaway(), cahootsWallet, stowawayContext);
        MultiCahoots multiCahoots6 = new MultiCahoots(multiCahoots5);
        multiCahoots6.setStowaway(stowaway4);
        multiCahoots6.setStep(6);
        return multiCahoots6;
    }

    @Override
    public void verifyResponse(MultiCahootsContext cahootsContext, MultiCahoots multiCahoots, MultiCahoots request) throws Exception {
        super.verifyResponse(cahootsContext, multiCahoots, request);

        if (multiCahoots.getStep() <= 4) {
            // validate stonewallx2
            CahootsContext stonewallContext = cahootsContext.getStonewallx2Context();
            stonewallx2Service.verifyResponse(stonewallContext, multiCahoots.stonewallx2, request.stonewallx2);
        }

        if (multiCahoots.getStep() >= 3) {
            // validate stowaway
            CahootsContext stowawayContext = cahootsContext.getStowawayContext();
            stowawayService.verifyResponse(stowawayContext, multiCahoots.stowaway, (request!=null?request.stowaway:null));
        } else {
            // stowaway should keep unchanged once finished
            if (!TxUtil.getInstance().getTxHex(multiCahoots.getStowawayTransaction())
                    .equals(TxUtil.getInstance().getTxHex(request.getStowawayTransaction()))) {
                throw new Exception("Invalid alterated stowaway tx");
            }
        }
    }
}
