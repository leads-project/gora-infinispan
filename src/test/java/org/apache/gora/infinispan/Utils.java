package org.apache.gora.infinispan;

import org.apache.gora.examples.generated.Employee;
import org.apache.gora.store.DataStore;

import java.util.Random;

/**
 * @author Pierre Sutra
 */
public class Utils {

   private static Random rand = new Random(System.currentTimeMillis());
   public static final long YEAR_IN_MS = 365L * 24L * 60L * 60L * 1000L;

   public static Employee createEmployee(int i) {
    Employee employee = Employee.newBuilder().build();
    employee.setSsn(Long.toString(i));
    employee.setName(Long.toString(rand.nextLong()));
    employee.setDateOfBirth(rand.nextLong() - 20L * YEAR_IN_MS);
    employee.setSalary(rand.nextInt());
    return employee;
  }

   public static <T extends CharSequence> void populateEmployeeStore(DataStore<T, Employee> dataStore, int n) {
         for(int i=0; i<n; i++) {
            Employee e = createEmployee(i);
            dataStore.put((T)e.getSsn(),e);
         }
         dataStore.flush();
   }

}
