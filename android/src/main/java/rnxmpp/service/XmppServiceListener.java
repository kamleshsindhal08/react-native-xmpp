package rnxmpp.service;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.forward.packet.Forwarded;

import rnxmpp.mam.MamElements;

/**
 * Created by Kristian Frølund on 7/19/16.
 * Updated by Faysal Ahmed on 03/28/18.
 * Copyright (c) 2016. Teletronics. All rights reserved
 */

public interface XmppServiceListener {
    void onError(Exception e);
    void onLoginError(String errorMessage);
    void onLoginError(Exception e);
    void onMessage(Message message);
    void onMessageSend(String stanzaId);
    void onRosterReceived(Roster roster);
    void onIQ(IQ iq);
    void onPresence(Presence presence);
    void onConnnect(String username, String password);
    void onDisconnect(Exception e);
    void onLogin(String username, String password);
    void onForwarded(MamElements.MamResultExtension result);
    
    // onMessageSend is supposed to implement the message send uniqueId
    // onForwarted is supposed to implement message history callback js

}
