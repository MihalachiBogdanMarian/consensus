package consensus.utilities;

import com.google.common.collect.Sets;
import consensus.eventhandlers.EpState;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

import java.io.*;
import java.util.*;

public class Utilities {

    public static byte[] intToBytes(final int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xff),
                (byte) ((value >> 16) & 0xff),
                (byte) ((value >> 8) & 0xff),
                (byte) (value & 0xff),
        };
    }

    public static int bytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length != 4) return 0x0;
        return ((0xff & bytes[0]) << 24 |
                (0xff & bytes[1]) << 16 |
                (0xff & bytes[2]) << 8 |
                (0xff & bytes[3])
        );
    }

    public static ProcessId maxrank(List<ProcessId> processes) {
        ProcessId maxProcess = processes.get(0);
        for (ProcessId process : processes) {
            if (process.getRank() > maxProcess.getRank()) {
                maxProcess = process;
            }
        }
        return maxProcess;
    }

    public static ProcessId maxrankWithoutSuspected(List<ProcessId> processes, Set<ProcessId> suspected) {
        Set processesWithoutSuspected = Sets.difference(new HashSet<>(processes), suspected);
        ProcessId maxProcess = ProcessId.newBuilder().setRank(0).build();
        if (!processesWithoutSuspected.isEmpty()) {
            Iterator<ProcessId> it = processesWithoutSuspected.iterator();
            while (it.hasNext()) {
                ProcessId process = it.next();
                if (process.getRank() > maxProcess.getRank()) {
                    maxProcess = process;
                }
            }
            return maxProcess;
        }
        return null;
    }

    public static ProcessId getProcessByAddress(List<ProcessId> processes, String senderHost, int senderListeningPort) {
        for (ProcessId process : processes) {
            if (process.getHost().equals(senderHost) && process.getPort() == senderListeningPort) {
                return process;
            }
        }
        return null;
    }

    public static void store(int value, String filename) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filename);
            fileWriter.write(String.valueOf(value));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int retrieve(String filename) {
        int value = -1;
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(reader);

            value = Integer.parseInt(bufferedReader.readLine());

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static void writeMessage(OutputStream out, Message message) {
        try {
            byte[] messageBytes = message.toByteArray();

//            String result = convertBytesToHexString(intToBytes(messageBytes.length));
//            result += convertBytesToHexString(messageBytes);
//            System.out.println(result);

            out.write(intToBytes(messageBytes.length), 0, Integer.SIZE / Byte.SIZE);
            out.write(messageBytes, 0, messageBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String convertBytesToHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte temp : bytes) {
            int decimal = (int) temp & 0xff;  // bytes widen to int, need mask, prevent sign extension
            String hex = Integer.toHexString(decimal);
            if (hex.length() == 1) {
                result.append("0" + hex);
            } else {
                result.append(hex);
            }
        }
        return result.toString();
    }

    public static Message readMessage(InputStream in) {
        try {
            byte[] lengthBytes = new byte[Integer.SIZE / Byte.SIZE];
            in.read(lengthBytes, 0, Integer.SIZE / Byte.SIZE);
            byte[] messageBytes = new byte[bytesToInt(lengthBytes)];
            in.read(messageBytes, 0, bytesToInt(lengthBytes));
            return Message.parseFrom(messageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int hashtag(Map<ProcessId, EpState> states) {
        int count = 0;
        for (Map.Entry<ProcessId, EpState> entry : states.entrySet()) {
            if (entry.getValue() != null) {
                count++;
            }
        }
        return count;
    }

    public static EpState highest(Map<ProcessId, EpState> states) {
        EpState epState = new EpState();
        for (Map.Entry<ProcessId, EpState> entry : states.entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getValue().getTimestamp() >= epState.getTimestamp()) {
                    epState = entry.getValue();
                }
            }
        }
        return epState;
    }

}
