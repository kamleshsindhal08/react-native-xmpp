package rnxmpp.service;

import android.support.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.RCTNativeAppEventEmitter;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smackx.forward.packet.Forwarded;

import rnxmpp.mam.MamElements;
import rnxmpp.mam.MamResultProvider;
import rnxmpp.utils.Parser;

/**
 * Created by Kristian Frølund on 7/19/16.
 * Copyright (c) 2016. Teletronics. All rights reserved
 */

public class RNXMPPCommunicationBridge implements XmppServiceListener {

    public static final String RNXMPP_ERROR =       "RNXMPPError";
    public static final String RNXMPP_LOGIN_ERROR = "RNXMPPLoginError";
    public static final String RNXMPP_MESSAGE =     "RNXMPPMessage";
    public static final String RNXMPP_MESSAGE_ERROR =     "RNXMPPMessageError";
    public static final String RNXMPP_MESSAGE_SEND ="RNXMPPMessageSend";
    public static final String RNXMPP_ROSTER =      "RNXMPPRoster";
    public static final String RNXMPP_IQ =          "RNXMPPIQ";
    public static final String RNXMPP_PRESENCE =    "RNXMPPPresence";
    public static final String RNXMPP_CONNECT =     "RNXMPPConnect";
    public static final String RNXMPP_DISCONNECT =  "RNXMPPDisconnect";
    public static final String RNXMPP_LOGIN =       "RNXMPPLogin";
    ReactContext reactContext;

    public RNXMPPCommunicationBridge(ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    @Override
    public void onError(Exception e) {
        sendEvent(reactContext, RNXMPP_ERROR, e.getLocalizedMessage());
    }

    @Override
    public void onLoginError(String errorMessage) {
        sendEvent(reactContext, RNXMPP_LOGIN_ERROR, errorMessage);
    }

    @Override
    public void onLoginError(Exception e) {
        this.onLoginError(e.getLocalizedMessage());
    }

    @Override
    public void onMessageError(String errorMessage) {
        sendEvent(reactContext, RNXMPP_MESSAGE_ERROR, errorMessage);
    }

    @Override
    public void onMessage(Message message) {
        // System.out.println("raw >>> "+ message.toXML().xmlnsAttribute());
        WritableMap params = Arguments.createMap();
        params.putString("thread", message.getThread());
        params.putString("subject", message.getSubject());
        params.putString("id", message.getStanzaId());
        params.putString("body", message.getBody());
        params.putString("from", message.getFrom());
        params.putString("src", message.toXML().toString());
//        System.out.println("** ** ** Message");
        sendEvent(reactContext, RNXMPP_MESSAGE, params);
    }

    @Override
    public void onMessageSend(String stanzaId) {
        sendEvent(reactContext, RNXMPP_MESSAGE_SEND, stanzaId);
    }

    @Override
    public void onForwarded(MamElements.MamResultExtension result){
        Forwarded forwarded = result.getForwarded();
        WritableMap params = Arguments.createMap();
        WritableMap resultObj = Arguments.createMap();
        WritableMap forwardedObj = Arguments.createMap();
        WritableMap delay = Arguments.createMap();
        WritableMap message = Arguments.createMap();

        params.putString("from", forwarded.getForwardedPacket().getFrom());

        delay.putString("stamp", forwarded.getDelayInformation().getStamp().toString());

        message.putString("body", ((Message) forwarded.getForwardedPacket()).getBody());
        message.putString("from", forwarded.getForwardedPacket().getFrom());
        message.putString("to", forwarded.getForwardedPacket().getTo());
        // params.putString("timestamp", forwarded.getDelayInformation().getStamp().toString());
        message.putString("src", forwarded.toXML().toString());
        message.putString("id", forwarded.getForwardedPacket().getStanzaId());

        // params.putString("forwarded", "true");
        System.out.println("** ** ** Forwarded");
        forwardedObj.putMap("delay", delay);
        forwardedObj.putMap("message", message);

        resultObj.putMap("forwarded", forwardedObj);
        params.putMap("result", resultObj);
        sendEvent(reactContext, RNXMPP_MESSAGE, params);

    }

    @Override
    public void onRosterReceived(Roster roster) {
        WritableArray rosterResponse = Arguments.createArray();
        for (RosterEntry rosterEntry : roster.getEntries()) {
            WritableMap rosterProps = Arguments.createMap();
            rosterProps.putString("username", rosterEntry.getUser());
            rosterProps.putString("displayName", rosterEntry.getName());
            WritableArray groupArray = Arguments.createArray();
            for (RosterGroup rosterGroup : rosterEntry.getGroups()) {
                groupArray.pushString(rosterGroup.getName());
            }
            rosterProps.putArray("groups", groupArray);
            rosterProps.putString("subscription", rosterEntry.getType().toString());
            rosterResponse.pushMap(rosterProps);
        }
        sendEvent(reactContext, RNXMPP_ROSTER, rosterResponse);
    }

    @Override
    public void onIQ(IQ iq) {
        sendEvent(reactContext, RNXMPP_IQ, Parser.parse(iq.toString()));
    }

    @Override
    public void onPresence(Presence presence) {
        WritableMap presenceMap = Arguments.createMap();
        presenceMap.putString("type", presence.getType().toString());
        presenceMap.putString("from", presence.getFrom());
        presenceMap.putString("status", presence.getStatus());
        presenceMap.putString("mode", presence.getMode().toString());
        sendEvent(reactContext, RNXMPP_PRESENCE, presenceMap);
    }

    @Override
    public void onConnnect(String username, String password) {
        WritableMap params = Arguments.createMap();
        params.putString("username", username);
        params.putString("password", password);
        sendEvent(reactContext, RNXMPP_CONNECT, params);
    }

    @Override
    public void onDisconnect(Exception e) {
        if (e != null) {
            sendEvent(reactContext, RNXMPP_DISCONNECT, e.getLocalizedMessage());
        } else {
            sendEvent(reactContext, RNXMPP_DISCONNECT, null);
        }
    }

    @Override
    public void onLogin(String username, String password) {
        WritableMap params = Arguments.createMap();
        params.putString("username", username);
        params.putString("password", password);
        sendEvent(reactContext, RNXMPP_LOGIN, params);
    }

    void sendEvent(ReactContext reactContext, String eventName, @Nullable Object params) {
        reactContext
                .getJSModule(RCTNativeAppEventEmitter.class)
                .emit(eventName, params);
    }
}
