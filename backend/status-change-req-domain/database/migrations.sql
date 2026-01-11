
-- add any new sql commands to this file after initalizing the database with the commands in schema.sql --

-- 12/2/25 - created 2 new tables to make maintability and organizing much easier
CREATE TABLE Returned_DE_Forms (
    Form_id INT PRIMARY KEY,
    FOREIGN KEY (Form_id) REFERENCES Forms(Form_id)
);

CREATE TABLE Returned_Review_Forms (
    Form_id INT PRIMARY KEY,
    FOREIGN KEY (Form_id) REFERENCES Forms(Form_id)
);

-- 12/3/25 - deleting a table and adding it as a row in an another table
DROP TABLE Deps_of_Imms;

ALTER TABLE Immigrants ADD COLUMN dependent_ids INTEGER[];

--Values and info
INSERT INTO public.review_forms
    (form_id, applicant_name, applicant_id,
     current_status, requested_status,
     reviewer_comments, workflow_status)
VALUES
    (1, 'John Boe', 'A123456789',
     'Tourist Visa (B-2)', 'Student Visa (F-1)',
     'Initial test row from pgAdmin', 'under_review');

SELECT * FROM public.review_forms;
