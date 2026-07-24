
CREATE TABlE pan_registry ( 
id int Primary key Not Null IDENTITY, 
pan_number VARCHAR(10) Not null Unique , 
holder_name VARCHAR(100),
mobile_number Varchar(15) unique , 
status VARCHAR(20),
addhar_linked BIT,
created_at DATETIME DEFAULT GETDATE()
);

INSERT INTO pan_registry 
( pan_number ,	holder_name, 
mobile_number, status, addhar_linked ) 
VALUES 
('NRNPS2927F', 'Saitej Shinde', '9322420432', 'ACTIVE' , 1 )  ;




CREATE TABLE aadhaar_registry
(
    id BIGINT PRIMARY KEY IDENTITY(1,1),

    aadhaar_number VARCHAR(12) UNIQUE NOT NULL,

    holder_name VARCHAR(100),

    dob DATE,

    gender VARCHAR(10),

    address VARCHAR(255),

    mobile_number VARCHAR(15),

    email VARCHAR(100),

    status VARCHAR(20),

    pan_linked BIT,

    created_at DATETIME DEFAULT GETDATE()
);


INSERT INTO aadhaar_registry
(

aadhaar_number, 
holder_name,
dob,
gender,
address,
mobile_number,
email,
status,
pan_linked
)
VALUES
(
'123456789101',
'Saitej Shinde',
'2026-12-19',
'Male',
'Shirdi Maharashtra',
'9322420432',
'saitej.g.shinde@gmail.com',
'ACTIVE',
1
);




CREATE TABLE otp_registry
(
    id INT IDENTITY(1,1) PRIMARY KEY,

    pan_number VARCHAR(10) NOT NULL,

    otp VARCHAR(6) NOT NULL,

    expiry_time DATETIME NOT NULL,

    verified BIT DEFAULT 0
);


ALTER TABLE pan_registry 
ADD email VARCHAR(100);