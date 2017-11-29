/* AUTO-GENERATED FILE.  DO NOT MODIFY.
 *
 * This class was automatically generated by the
 * java mavlink generator tool. It should not be modified by hand.
 */

package com.MAVLink;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkStats;

/**
 * MAVLink parser that parses @{link MAVLinkPacket}s from a byte stream one byte
 * at a time. 
 *
 * After creating an instance of this class, simply just use the @{link #mavlink_parse_char} 
 * method to parse a byte stream.
 */
public class Parser {

    /**
     * States from the parsing state machine
     */
    enum MAV_states {
        MAVLINK_PARSE_STATE_UNINIT,
        MAVLINK_PARSE_STATE_IDLE,
        MAVLINK_PARSE_STATE_GOT_STX,
        MAVLINK_PARSE_STATE_GOT_LENGTH,
        MAVLINK_PARSE_STATE_GOT_INCOMPAT_FLAGS,
        MAVLINK_PARSE_STATE_GOT_COMPAT_FLAGS,
        MAVLINK_PARSE_STATE_GOT_SEQ,
        MAVLINK_PARSE_STATE_GOT_SYSID,
        MAVLINK_PARSE_STATE_GOT_COMPID,
        MAVLINK_PARSE_STATE_GOT_MSGID1,
        MAVLINK_PARSE_STATE_GOT_MSGID2,
        MAVLINK_PARSE_STATE_GOT_MSGID3,
        MAVLINK_PARSE_STATE_GOT_CRC1,
        MAVLINK_PARSE_STATE_GOT_CRC2,
        MAVLINK_PARSE_STATE_GOT_PAYLOAD,
        MAVLINK_PARSE_STATE_GOT_SIGNATURE,
    }

    private MAV_states state = MAV_states.MAVLINK_PARSE_STATE_UNINIT;

    public MAVLinkStats stats;
    private MAVLinkPacket m;
    private boolean isMavlink2;

    public Parser() {
        this(false);
    }

    public Parser(boolean ignoreRadioPacketStats) {
        stats = new MAVLinkStats(ignoreRadioPacketStats);
        isMavlink2 = false;
    }

    /**
     * This is a convenience function which handles the complete MAVLink
     * parsing. the function will parse one byte at a time and return the
     * complete packet once it could be successfully decoded. Checksum and other
     * failures will be silently ignored.
     * 
     * @param c The char to parse
     * @return the complete @{link MAVLinkPacket} if successfully decoded, else null
     */
    public MAVLinkPacket mavlink_parse_char(int c) {

        // for code clarity, parsing is handled differently depending on if it's a MAVLink1 or MAVLink2 frame
        if(isMavlink2) {
            // MAVLINK 2 frame
            switch (state) {
                case MAVLINK_PARSE_STATE_UNINIT:
                case MAVLINK_PARSE_STATE_IDLE:
                    if (c == MAVLinkPacket.MAVLINK2_STX) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                        isMavlink2 = true;
                    } else if (c == MAVLinkPacket.MAVLINK1_STX) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                        isMavlink2 = false;
                    }
                    break;

                case MAVLINK_PARSE_STATE_GOT_STX:
                    m = new MAVLinkPacket(c, true);
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_LENGTH;
                    break;

                case MAVLINK_PARSE_STATE_GOT_LENGTH:
                    m.incompatFlags = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_INCOMPAT_FLAGS;
                    break;

                case MAVLINK_PARSE_STATE_GOT_INCOMPAT_FLAGS:
                    m.compatFlags = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_COMPAT_FLAGS;
                    break;

                case MAVLINK_PARSE_STATE_GOT_COMPAT_FLAGS:
                    m.seq = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_SEQ;
                    break;

                case MAVLINK_PARSE_STATE_GOT_SEQ:
                    m.sysid = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_SYSID;
                    break;

                case MAVLINK_PARSE_STATE_GOT_SYSID:
                    m.compid = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_COMPID;
                    break;

                case MAVLINK_PARSE_STATE_GOT_COMPID:
                    m.msgid = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_MSGID1;
                    break;

                case MAVLINK_PARSE_STATE_GOT_MSGID1:
                    m.msgid |= c << 8;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_MSGID2;
                    break;

                case MAVLINK_PARSE_STATE_GOT_MSGID2:
                    m.msgid |= c << 16;
                    if (m.len == 0) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_PAYLOAD;
                    } else {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_MSGID3;
                    }
                    break;

                case MAVLINK_PARSE_STATE_GOT_MSGID3:
                    m.payload.add((byte) c);
                    if (m.payloadIsFilled()) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_PAYLOAD;
                    }
                    break;

                case MAVLINK_PARSE_STATE_GOT_PAYLOAD:
                    m.generateCRC();
                    // Check first checksum byte
                    if (c != m.crc.getLSB()) {
                        state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                        stats.crcError();
                    } else {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_CRC1;
                    }
                    break;

                case MAVLINK_PARSE_STATE_GOT_CRC1:
                    // Check second checksum byte
                    if (c != m.crc.getMSB()) {
                        state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                        stats.crcError();
                    } else { // crc is good
                        stats.newPacket(m);
                        m.signature = new Signature();
//                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_CRC2;
                        // Successfully received the message
                        state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                        return m;
                    }

                    break;
                // TODO: implement signature parsing and validation
//                case MAVLINK_PARSE_STATE_GOT_CRC2:
//                    m.signature.signature.put((byte) c);
//                    if(m.signature.signature.position() == Signature.MAX_SIGNATURE_SIZE) {
//                        state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
//                        // Successfully received the message
//                        return m;
//                    }
//                    break;

            }
        } else {
            // MAVLINK 1 frame
            switch (state) {
                case MAVLINK_PARSE_STATE_UNINIT:
                case MAVLINK_PARSE_STATE_IDLE:
                    if (c == MAVLinkPacket.MAVLINK2_STX) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                        isMavlink2 = true;
                    } else if (c == MAVLinkPacket.MAVLINK1_STX) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                        isMavlink2 = false;
                    }
                    break;

                case MAVLINK_PARSE_STATE_GOT_STX:
                    m = new MAVLinkPacket(c);
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_LENGTH;
                    break;

                case MAVLINK_PARSE_STATE_GOT_LENGTH:
                    m.seq = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_SEQ;
                    break;

                case MAVLINK_PARSE_STATE_GOT_SEQ:
                    m.sysid = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_SYSID;
                    break;

                case MAVLINK_PARSE_STATE_GOT_SYSID:
                    m.compid = c;
                    state = MAV_states.MAVLINK_PARSE_STATE_GOT_COMPID;
                    break;

                case MAVLINK_PARSE_STATE_GOT_COMPID:
                    m.msgid = c;
                    if (m.len == 0) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_PAYLOAD;
                    } else {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_MSGID1;
                    }
                    break;

                case MAVLINK_PARSE_STATE_GOT_MSGID1:
                    m.payload.add((byte) c);
                    if (m.payloadIsFilled()) {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_PAYLOAD;
                    }
                    break;

                case MAVLINK_PARSE_STATE_GOT_PAYLOAD:
                    m.generateCRC();
                    // Check first checksum byte
                    if (c != m.crc.getLSB()) {
                        state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                        if (c == MAVLinkPacket.MAVLINK1_STX) {
                            state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                            m.crc.start_checksum();
                        }
                        stats.crcError();
                    } else {
                        state = MAV_states.MAVLINK_PARSE_STATE_GOT_CRC1;
                    }
                    break;

                case MAVLINK_PARSE_STATE_GOT_CRC1:
                    // Check second checksum byte
                    if (c != m.crc.getMSB()) {
                        state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                        if (c == MAVLinkPacket.MAVLINK1_STX) {
                            state = MAV_states.MAVLINK_PARSE_STATE_GOT_STX;
                            m.crc.start_checksum();
                        }
                        stats.crcError();
                    } else { // Successfully received the message
                        stats.newPacket(m);
                        state = MAV_states.MAVLINK_PARSE_STATE_IDLE;
                        return m;
                    }

                    break;
            }
        }
        return null;
    }
}
