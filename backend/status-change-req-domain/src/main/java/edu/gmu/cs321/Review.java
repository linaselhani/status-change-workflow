package edu.gmu.cs321;

public class Review {

    // vars should be set to null or true
    private Form form;
    private boolean noErrors;
    private boolean isValid;

    // almost ALWAYS use this
    public Review(){
        // initialze variables
    }

    // should RARELY use this
    public Review(Form form, boolean noErrors, boolean isValid){
        //initialize variables
    }


    
    /*** Add in getters and setters for all parameters in second Review Constructor ***/



    /** notifies data entry user that form is invalid. 
     * @return true if successfully notified. returns false if failed.
     */
    public boolean informDE(){
        return true;
    }

    /** notifies approver that a form has been reviewed and added to the workflow.
     * @return true if successfully notified approval ; false if failed;
     */
    public boolean informReviewer(){
        return true;
    }

    /** gets a form object from the workflow and sets this.form equal to it. 
     * 
     * @return true if successfully retrieved from workflow ; false if failed
     */
    public boolean getFromWF(){
        return true;
    }

    /** adds form to workflow. 
     * if form is valid uses informApproval().
     * if form is invalid, uses informDE().
     * @return true if successfully added ; false if it failed
     */ 
    public boolean addToWF(){
        return true;
    }

}
