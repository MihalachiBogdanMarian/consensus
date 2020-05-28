import basics.ListPeople;
import consensus.protos.Consensus;
import consensus.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

//    public static Queue<AbstractEvent> eventsQueue = new Queue<>();
//    public static List<ProcessId> processes = new ArrayList<>() {{
//        add(ProcessId.newBuilder().setHost("127.0.0.1").setPort(8001).setOwner("Bogdan").setIndex(1).build());
//        add(ProcessId.newBuilder().setHost("127.0.0.1").setPort(8002).setOwner("Bogdan").setIndex(2).build());
//        add(ProcessId.newBuilder().setHost("127.0.0.1").setPort(8003).setOwner("Bogdan").setIndex(3).build());
//        add(ProcessId.newBuilder().setHost("127.0.0.1").setPort(8004).setOwner("Bogdan").setIndex(4).build());
//    }};

    public static void main(String[] args) {
        System.out.println("ep0".split("ep")[1]);
//        EventsThread eventsThread = new EventsThread("EventsThread");
//        eventsThread.start();
//
//
//        eventsQueue.insert(new BebBroadcast());
//        eventsQueue.insert(new PlSend());
//        eventsQueue.getHead().getObject().setCondition(false);
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        eventsQueue.getHead().getObject().setCondition(true);
//
//
//        Person john =
//                Person.newBuilder()
//                        .setId(1234)
//                        .setName("John Doe")
//                        .setEmail("jdoe@example.com")
//                        .addPhones(
//                                Person.PhoneNumber.newBuilder()
//                                        .setNumber("555-4321")
//                                        .setType(Person.PhoneType.HOME))
//                        .build();
//
//        Person tim =
//                Person.newBuilder()
//                        .setId(5678)
//                        .setName("Tim Pop")
//                        .setEmail("tmain@example.com")
//                        .addPhones(
//                                Person.PhoneNumber.newBuilder()
//                                        .setNumber("999-8765")
//                                        .setType(Person.PhoneType.MOBILE))
//                        .build();
//
//        System.out.println(john.toString());
//        System.out.println(tim.toString());
//        System.out.println(Integer.SIZE);

        try {
//            AddPerson.addPersonToAddressFile("C:\\Users\\BiDi\\Documents\\IntelliJProjects\\consensus\\src\\main\\resources\\persons.txt");
            ListPeople.listPeople("C:\\Users\\BiDi\\Documents\\IntelliJProjects\\consensus\\src\\main\\resources\\persons.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
