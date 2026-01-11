package edu.gmu.cs321;

public class DataEntry {

    private Form form;

    public DataEntry(Form form){
        //initalize variables
    }



    /*** Add in getters and setters for all parameters in DataEntry Constructor ***/

    

    // adds form to workflow. returns true if successfully added and false if it failed
    public boolean addToWF(){
        return true; 
    }

    // gets a form from workflow that was added by the reviewer as an invalid form
    public Form getNextInvalidForm(){
        return null;
    }

}
