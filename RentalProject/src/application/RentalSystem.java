package application;
import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RentalSystem {
	private static RentalSystem rental;
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();
    
    private RentalSystem() {
    	loadData();
    }
    
    public boolean addVehicle(Vehicle vehicle) {
        if (findVehicleByPlate(vehicle.getLicensePlate()) != null) {
            System.out.println("A vehicle with license plate " + vehicle.getLicensePlate() + " already exists.");
            return false;
        }

        vehicles.add(vehicle);
        saveVehicle(vehicle);
        return true;
    }
    
    public void saveVehicle(Vehicle vehicle) {
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter("vehicles.txt", true))) {
            writer.write(vehicle.getClass().getSimpleName() + " " + vehicle.getInfo() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static RentalSystem getInstance() {
    	if(rental == null) {
    		rental = new RentalSystem();
    	}
    	return rental;
    }

    public boolean addCustomer(Customer customer) {
        if (findCustomerById(String.valueOf(customer.getCustomerId())) != null) {
            System.out.println("A customer with ID " + customer.getCustomerId() + " already exists.");
            return false;
        }

        customers.add(customer);
        saveCustomer(customer);
        return true;
    }
    
    public void saveCustomer(Customer customer) {
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter("customers.txt", true))) {
            writer.write(customer + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
            vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
            rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, amount, "RENT"));
            saveRecord(new RentalRecord(vehicle, customer, date, amount, "RENT"));
            System.out.println("Vehicle rented to " + customer.getCustomerName());
            
        }
        else {
            System.out.println("Vehicle is not available for renting.");
        }
    }

    public void returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.RENTED) {
            vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
            rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, extraFees, "RETURN"));
            saveRecord(new RentalRecord(vehicle, customer, date, extraFees, "RETURN"));
            System.out.println("Vehicle returned by " + customer.getCustomerName());
        }
        else {
            System.out.println("Vehicle is not rented.");
        }
    }    
    
    public void saveRecord(RentalRecord record) {
    	try (BufferedWriter writer = new BufferedWriter(new FileWriter("rental_records.txt", true))) {
            writer.write(record + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayVehicles(boolean onlyAvailable) {
    	System.out.println("|     Type         |\tPlate\t|\tMake\t|\tModel\t|\tYear\t|");
    	System.out.println("---------------------------------------------------------------------------------");
    	 
        for (Vehicle v : vehicles) {
            if (!onlyAvailable || v.getStatus() == Vehicle.VehicleStatus.AVAILABLE) {
                System.out.println("|     " + (v instanceof Car ? "Car          " : "Motorcycle   ") + "|\t" + v.getLicensePlate() + "\t|\t" + v.getMake() + "\t|\t" + v.getModel() + "\t|\t" + v.getYear() + "\t|\t");
            }
        }
        System.out.println();
    }
    
    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        for (RentalRecord record : rentalHistory.getRentalHistory()) {
            System.out.println(record.toString());
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(String id) {
        for (Customer c : customers)
            if (c.getCustomerId() == Integer.parseInt(id))
                return c;
        return null;
    }
    
    public Customer findCustomerByName(String name) {
        for (Customer c : customers) {
            if (c.getCustomerName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }
    
    private void loadData() {
    	try (BufferedReader vreader = new BufferedReader(new FileReader("vehicles.txt"))) {
            String line;
            while ((line = vreader.readLine()) != null) {
            	line = line.trim();
            	String[] parts = line.replaceAll("^\\|?\\s*", "").replaceAll("\\s*\\|\\s*$", "").split("\\s*\\|\\s*");
                if (parts.length != 7) continue;
                String type = parts[0];
                String plate = parts[1];
                String make = parts[2];
                String model = parts[3];
                int year = Integer.parseInt(parts[4]);
                Vehicle.VehicleStatus status = Vehicle.VehicleStatus.valueOf(parts[5]);
                String extraDets = parts[6];

                Vehicle vehicle;
                if (type.equalsIgnoreCase("Car")) {
                    vehicle = new Car(make, model, year, Integer.parseInt(extraDets));
                } else if (type.equalsIgnoreCase("MotorCycle")){
                    vehicle = new Motorcycle(make, model, year, Boolean.parseBoolean(extraDets));
                } else {
                	vehicle = new Truck(make, model, year, Double.parseDouble(extraDets));
                }
                vehicle.setStatus(status);
                vehicle.setLicensePlate(plate);
                vehicles.add(vehicle);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	try (BufferedReader creader = new BufferedReader(new FileReader("customers.txt"))) {
            String line;
            while ((line = creader.readLine()) != null) {
            	String[] parts = line.split(" \\| ");
                if (parts.length != 2) continue;

                int id = Integer.parseInt(parts[0].replace("Customer ID: ", "").trim());
                String name = parts[1].replace("Name: ", "").trim();

                Customer customer = new Customer(id, name);
                customers.add(customer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
    	try (BufferedReader rreader = new BufferedReader(new FileReader("rental_records.txt"))) {
            String line;
            while ((line = rreader.readLine()) != null) {
            	String[] parts = line.split(" \\| ");
                if (parts.length != 5) continue;

                String recordType = parts[0].trim();
                String plate = parts[1].replace("Plate: ", "").trim();
                String customerName = parts[2].replace("Customer: ", "").trim();
                LocalDate date = LocalDate.parse(parts[3].replace("Date: ", "").trim());
                double amount = Double.parseDouble(parts[4].replace("Amount: $", "").trim());

                Vehicle vehicle = findVehicleByPlate(plate);
                Customer customer = findCustomerByName(customerName); 

                if (vehicle != null && customer != null) {
                    if (recordType.equalsIgnoreCase("RENT")) {
                        vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
                    } else if (recordType.equalsIgnoreCase("RETURN")) {
                        vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
                    }

                    rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, amount, recordType));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}