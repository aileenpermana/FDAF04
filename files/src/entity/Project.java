package entity;

import control.HDBOfficerControl;
import control.ProjectControl;
import java.util.*;

/**
 * Represents a BTO Project in the system.
 * Demonstrates encapsulation with private fields and public getters/setters.
 */
public class Project {
    private String projectID;
    private String projectName;
    private String neighborhood;
    private Map<FlatType, Integer> totalUnits;
    private Map<FlatType, Integer> availableUnits;
    private Date applicationOpenDate;
    private Date applicationCloseDate;
    private HDBManager managerInCharge;
    private List<HDBOfficer> officers;
    private int maxOfficerSlots;
    private int availableOfficerSlots;
    private boolean isVisible;
    
    
    public Project(String projectID, String projectName, String neighborhood,
                   Map<FlatType, Integer> totalUnits, Date openDate, Date closeDate,
                   HDBManager manager, int officerSlots) {
        this.projectID = projectID;
        this.projectName = projectName;
        this.neighborhood = neighborhood;
        this.totalUnits = new HashMap<>(totalUnits);
        this.availableUnits = new HashMap<>(totalUnits); // Initially all units are available
        this.applicationOpenDate = openDate;
        this.applicationCloseDate = closeDate;
        this.managerInCharge = manager;
        this.officers = new ArrayList<>();
        this.maxOfficerSlots = officerSlots;
        this.availableOfficerSlots = officerSlots;
        this.isVisible = true; // Default to visible
    }

    public String getProjectID() {
        return projectID;
    }

    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getNeighborhood() {
        return neighborhood;
    }
    

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    

    public Set<FlatType> getFlatTypes() {
        return totalUnits.keySet();
    }
    

    public List<FlatType> getFlatTypesList() {
        return new ArrayList<>(totalUnits.keySet());
    }
    
    /**
     * Check if this project has a specific flat type
     * @param type the flat type to check
     * @return true if the project has this flat type, false otherwise
     */
    public boolean hasFlatType(FlatType type) {
        return totalUnits.containsKey(type) && totalUnits.get(type) > 0;
    }
    
    /**
     * Get total units for a flat type
     * @param type the flat type
     * @return number of units, 0 if type not available
     */
    public int getTotalUnitsByType(FlatType type) {
        return totalUnits.getOrDefault(type, 0);
    }
    
    /**
     * Set the number of units for a flat type
     * @param type the flat type
     * @param count the new count
     */
    public void setNumberOfUnitsByType(FlatType type, int count) {
        // Update total units
        totalUnits.put(type, count);
        
        // Update available units (can't be more than total)
        int currentAvailable = availableUnits.getOrDefault(type, 0);
        availableUnits.put(type, Math.min(currentAvailable, count));
    }
    
    /**
     * Get available units for a flat type
     * @param type the flat type
     * @return number of available units, 0 if type not available
     */
    public int getAvailableUnitsByType(FlatType type) {
        return availableUnits.getOrDefault(type, 0);
    }


    public void printProjectState() {
        System.out.println("Project State for: " + projectName + " (ID: " + projectID + ")");
        System.out.println("Neighborhood: " + neighborhood);
        System.out.println("Flat Types and Units:");
        
        for (FlatType type : FlatType.values()) {
            int total = totalUnits.getOrDefault(type, 0);
            int available = availableUnits.getOrDefault(type, 0);
            
            if (total > 0) {
                System.out.println("- " + type.getDisplayValue() + ": " + 
                                 available + " available out of " + total + " total");
            }
        }
        
        System.out.println("Officer Slots: " + availableOfficerSlots + " available out of " + maxOfficerSlots);
        System.out.println("Visibility: " + (isVisible ? "Visible" : "Hidden"));
    }
    
    
    public void setAvailableUnitsByType(FlatType type, int count) {
        // Can't have more available units than total units
        int total = totalUnits.getOrDefault(type, 0);
        if (count > total) {
            count = total;
        }
        
        // Can't have negative available units
        if (count < 0) {
            count = 0;
        }
        
        // Update the map
        availableUnits.put(type, count);
        
        // Log the update for debugging
    }
    
    
    public boolean decrementAvailableUnits(FlatType type) {
        // Get current available units
        Integer available = availableUnits.get(type);
        
        // If no entry exists for this type or available units <= 0
        if (available == null || available <= 0) {
            System.out.println("Cannot decrement: No available units of type " + type.getDisplayValue());
            return false;
        }
        
        // Decrement available units
        int newAvailable = available - 1;
        availableUnits.put(type, newAvailable);
        
        // Log the update for debugging
        System.out.println("Decremented available units for " + type.getDisplayValue() + 
                         " from " + available + " to " + newAvailable);
        
        ProjectControl projectControl = new ProjectControl();
        projectControl.updateProjectUnitsAfterBooking(this, type);
        
        return true;
    }
    
    
    /**
     * Increment available units for a flat type
     * @param type the flat type
     * @return true if successful, false if already at maximum
     */
    public boolean incrementAvailableUnits(FlatType type) {
        int available = availableUnits.getOrDefault(type, 0);
        int total = totalUnits.getOrDefault(type, 0);
        
        if (available >= total) {
            return false;
        }
        
        availableUnits.put(type, available + 1);
        return true;
    }
    
    /**
     * Get the application opening date
     * @return opening date
     */
    public Date getApplicationOpenDate() {
        return applicationOpenDate;
    }
    
    /**
     * Set the application opening date
     * @param date new opening date
     */
    public void setApplicationOpenDate(Date date) {
        this.applicationOpenDate = date;
    }
    
    /**
     * Get the application closing date
     * @return closing date
     */
    public Date getApplicationCloseDate() {
        return applicationCloseDate;
    }
    
    /**
     * Set the application closing date
     * @param date new closing date
     */
    public void setApplicationCloseDate(Date date) {
        this.applicationCloseDate = date;
    }
    
    /**
 * Check if the project is open for application
 * @return true if within application period, false otherwise
 */
public boolean isOpenForApplication() {
    Date now = new Date();
    
    // Project must be visible
    if (!isVisible()) {
        return false;
    }
    
    // Current date must be between open and close dates (inclusive)
    return now.compareTo(applicationOpenDate) >= 0 && now.compareTo(applicationCloseDate) <= 0;
}
    
    /**
     * Get the manager in charge
     * @return manager in charge
     */
    public HDBManager getManagerInCharge() {
        return managerInCharge;
    }
    
    /**
     * Get the list of officers assigned to this project
     * @return list of officers
     */
    public List<HDBOfficer> getOfficers() {
        return new ArrayList<>(officers);
    }
    
    /**
     * Add an officer to this project
     * @param officer the officer to add
     * @return true if addition was successful, false otherwise
     */
    public boolean addOfficer(HDBOfficer officer) {
        if (availableOfficerSlots <= 0) {
            return false;
        }
        
        if (!officers.contains(officer)) {
            officers.add(officer);
            availableOfficerSlots--;
            return true;
        }
        
        return false;
    }
    
    /**
     * Remove an officer from this project
     * @param officer the officer to remove
     * @return true if removal was successful, false otherwise
     */
    public boolean removeOfficer(HDBOfficer officer) {
        if (officers.remove(officer)) {
            availableOfficerSlots++;
            return true;
        }
        
        return false;
    }
    
    /**
     * Set the maximum number of officer slots
     * @param slots the new maximum
     */
    public void setOfficerSlots(int slots) {
        // Ensure slots is at least equal to the number of current officers
        this.maxOfficerSlots = Math.max(slots, officers.size());
        this.availableOfficerSlots = maxOfficerSlots - officers.size();
    }
    
    /**
     * Get the available officer slots
     * @return available slots
     */
    public int getAvailableOfficerSlots() {
        return availableOfficerSlots;
    }
    
    /**
     * Decrement available officer slots
     * @return true if successful, false if no slots available
     */
    public boolean decrementOfficerSlots() {
        if (availableOfficerSlots <= 0) {
            return false;
        }
        
        availableOfficerSlots--;
        return true;
    }
    
    /**
     * Increment available officer slots
     * @return true if successful, false if already at maximum
     */
    public boolean incrementOfficerSlots() {
        if (availableOfficerSlots >= maxOfficerSlots) {
            return false;
        }
        
        availableOfficerSlots++;
        return true;
    }
    
    /**
     * Check if the project is visible
     * @return visibility status
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Set the project visibility
     * @param visible new visibility
     */
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }
    
    /**
     * Check if a user is eligible for this project
     * @param user the user to check
     * @return true if eligible, false otherwise
     */
    public boolean checkEligibility(User user, String projectID) {
        // Check if the project is open for application
        if (!isOpenForApplication()) {
            return false;
        }
        
        
        
        // Check if the user is an applicant
        if (!(user instanceof Applicant)) {
            return false; // Only applicants can apply
        }
    
        HDBOfficerControl officerControl = new HDBOfficerControl();
        List<Map<String, Object>> officerRegistrations = officerControl.getOfficerRegistrations(
                new HDBOfficer(
                    user.getName(),
                    user.getNRIC(),
                    user.getPassword(),
                    user.getAge(),
                    user.getMaritalStatus(),
                    "HDBOfficer"
                )
            );
            if (!officerRegistrations.isEmpty()) {
                for (Map<String, Object> reg : officerRegistrations) {
                    Project project = (Project) reg.get("project");
                    RegistrationStatus status = (RegistrationStatus) reg.get("status");
                    if (status == RegistrationStatus.APPROVED){
                        if (project.getProjectID().equals(projectID)) {
                            return false; // Already registered for this project
                        }
                    }
                }
            }
    
    
    // Check age and marital status
    int age = user.getAge();
    MaritalStatus maritalStatus = user.getMaritalStatus();
    
    if (maritalStatus == MaritalStatus.SINGLE) {
        // Singles must be 35 years or older and project must have 2-Room units
        return age >= 35 && totalUnits.containsKey(FlatType.TWO_ROOM);
    } else if (maritalStatus == MaritalStatus.MARRIED) {
        // Married couples must be 21 years or older
        return age >= 21;
    }
    
    return false;
}
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Project project = (Project) obj;
        return projectID.equals(project.projectID);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(projectID);
    }
}