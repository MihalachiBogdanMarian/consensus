package consensus.utilities;

import consensus.network.process.EpState;
import consensus.network.process.Process;
import consensus.protos.Consensus.ProcessId;
import consensus.protos.Consensus.Message;

import java.io.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

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

    public static int rank(List<ProcessId> processes, ProcessId process) {
        for (ProcessId p : processes) {
            if (p.equals(process)) {
                return p.getIndex();
            }
        }
        return 0;
    }

    public static ProcessId maxrank(List<ProcessId> processes) {
        ProcessId maxProcess = processes.get(0);
        for (ProcessId process : processes) {
            if (process.getIndex() > maxProcess.getIndex()) {
                maxProcess = process;
            }
        }
        return maxProcess;
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

    public static void writeProcess(OutputStream out, ProcessId process) {
        try {
            byte[] processIdBytes = process.toByteArray();
            out.write(intToBytes(processIdBytes.length), 0, Integer.SIZE / Byte.SIZE);
            out.write(processIdBytes, 0, processIdBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMessage(OutputStream out, Message message) {
        try {
            byte[] messageBytes = message.toByteArray();
            out.write(intToBytes(messageBytes.length), 0, Integer.SIZE / Byte.SIZE);
            out.write(messageBytes, 0, messageBytes.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ProcessId readProcess(InputStream in) {
        try {
            byte[] lengthBytes = new byte[Integer.SIZE / Byte.SIZE];
            in.read(lengthBytes, 0, Integer.SIZE / Byte.SIZE);
            byte[] processIdBytes = new byte[bytesToInt(lengthBytes)];
            in.read(processIdBytes, 0, bytesToInt(lengthBytes));
            return ProcessId.parseFrom(processIdBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public static ProcessId select(LinkedList<SimpleEntry<ProcessId, Integer>> candidates) {
        // select the max rank process from the ones with the minimum epoch timestamp
        // because they were inserted such that it keeps the order, we need to get the first element
        return candidates.get(0).getKey();
    }

    public static boolean exists(LinkedList<SimpleEntry<ProcessId, Integer>> candidates, ProcessId process, Integer value) {
        for (SimpleEntry<ProcessId, Integer> simpleEntry : candidates) {
            if (simpleEntry.getKey().equals(process) && simpleEntry.getValue() < value) {
                return true;
            }
        }
        return false;
    }

    public static boolean addInOrder(LinkedList<SimpleEntry<ProcessId, Integer>> linkedList, ProcessId process, Integer value) {
        ListIterator<SimpleEntry<ProcessId, Integer>> listIterator = linkedList.listIterator();

        while (listIterator.hasNext()) {
            int comparison = compareTo(listIterator.next(), new SimpleEntry<>(process, value));
            if (comparison == 0) {
                // equal, do not add
                return false;
            } else if (comparison > 0) {
                // new element should appear before this one
                listIterator.previous();
                listIterator.add(new SimpleEntry<>(process, value));
                return true;
            } else {
                // move on to the next element
            }
        }

        listIterator.add(new SimpleEntry<>(process, value));
        return true;
    }

    private static int compareTo(SimpleEntry<ProcessId, Integer> simpleEntry1, SimpleEntry<ProcessId, Integer> simpleEntry2) {
        if (Utilities.rank(Process.processes, simpleEntry1.getKey()) == Utilities.rank(Process.processes, simpleEntry2.getKey())
                && simpleEntry1.getValue() == simpleEntry2.getValue()) {
            return 0;
        } else if ((simpleEntry1.getValue() > simpleEntry2.getValue()) ||
                (Utilities.rank(Process.processes, simpleEntry1.getKey()) < Utilities.rank(Process.processes, simpleEntry2.getKey())
                        && simpleEntry1.getValue() == simpleEntry2.getValue())) {
            return 1;
        } else if ((simpleEntry1.getValue() < simpleEntry2.getValue()) ||
                (Utilities.rank(Process.processes, simpleEntry1.getKey()) > Utilities.rank(Process.processes, simpleEntry2.getKey())
                        && simpleEntry1.getValue() == simpleEntry2.getValue())) {
            return -1;
        }
        return 0;
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
