package basics;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.Person;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;

public class AddPerson {

    // This function fills in a Person message based on user input.
    static Person promptForAddress(BufferedReader stdin,
                                   PrintStream stdout) throws IOException {
        Person.Builder person = Person.newBuilder();

        stdout.print("Enter person ID: ");
        person.setId(Integer.parseInt(stdin.readLine()));

        stdout.print("Enter name: ");
        person.setName(stdin.readLine());

        stdout.print("Enter email address (blank for none): ");
        String email = stdin.readLine();
        if (email.length() > 0) {
            person.setEmail(email);
        }

        while (true) {
            stdout.print("Enter a phone number (or leave blank to finish): ");
            String number = stdin.readLine();
            if (number.length() == 0) {
                break;
            }

            Person.PhoneNumber.Builder phoneNumber =
                    Person.PhoneNumber.newBuilder().setNumber(number);

            stdout.print("Is this a mobile, home, or work phone? ");
            String type = stdin.readLine();
            switch (type) {
                case "mobile":
                    phoneNumber.setType(Person.PhoneType.MOBILE);
                    break;
                case "home":
                    phoneNumber.setType(Person.PhoneType.HOME);
                    break;
                case "work":
                    phoneNumber.setType(Person.PhoneType.WORK);
                    break;
                default:
                    stdout.println("Unknown phone type. Using default.");
                    break;
            }

            person.addPhones(phoneNumber);
        }

        return person.build();
    }

    // Main function: Reads the entire address book from a file,
    // adds one person based on user input, then writes it back out to the same
    // file.
    public static void addPersonToAddressFile(String pathToFile) throws Exception {

        AddressBook.Builder addressBook = AddressBook.newBuilder();

        // Read the existing address book.
        try {
            addressBook.mergeFrom(new FileInputStream(pathToFile));
        } catch (FileNotFoundException e) {
            System.out.println(pathToFile + ": File not found. Creating a new file.");
        }

        // Add an address.
        addressBook.addPeople(
                promptForAddress(new BufferedReader(new InputStreamReader(System.in)),
                        System.out));

        // Write the new address book back to disk.
        FileOutputStream output = new FileOutputStream(pathToFile);
        addressBook.build().writeTo(output);
        output.close();
    }

}
