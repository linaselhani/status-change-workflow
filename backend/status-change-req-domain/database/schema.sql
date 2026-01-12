CREATE TABLE Persons (
	id SERIAL PRIMARY KEY,
    first_name VARCHAR(25) NOT NULL,
    last_name VARCHAR(25) NOT NULL,
    Dob DATE NOT NULL
);

CREATE TABLE Dependents (
	id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES Persons(id)
);

CREATE TABLE Immigrants (
	id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES Persons(id),
    cur_status VARCHAR(50) NOT NULL
);

CREATE TABLE Deps_of_Imms (
	Imm_id INT NOT NULL,
    Dep_id INT,
    PRIMARY KEY (Imm_id, Dep_id),
    FOREIGN KEY (Imm_id) REFERENCES Immigrants(id),
    FOREIGN KEY (Dep_id) REFERENCES Dependents(id)
);

CREATE TABLE Forms (
	Form_id SERIAL PRIMARY KEY,
    Imm_id INT NOT NULL,
    req_status VARCHAR(50) NOT NULL,
    return_reason TEXT,
    FOREIGN KEY (Imm_id) REFERENCES Immigrants(id)
);

CREATE TABLE Data_Entry_Forms (
    Form_id INT PRIMARY KEY,
    FOREIGN KEY (Form_id) REFERENCES Forms(Form_id)
);

CREATE TABLE Review_Forms (
    Form_id INT PRIMARY KEY,
    FOREIGN KEY (Form_id) REFERENCES Forms(Form_id)
);

CREATE TABLE Approval_Forms (
    Form_id INT PRIMARY KEY,
    FOREIGN KEY (Form_id) REFERENCES Forms(Form_id)
);

CREATE TABLE Completed_Forms (
    Form_id INT PRIMARY KEY,
    FOREIGN KEY (Form_id) REFERENCES Forms(Form_id)
);

