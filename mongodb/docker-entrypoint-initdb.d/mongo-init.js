const fs = require('fs');

print('#---------------------------------------------');
print('Create new user');

// Read username and password from secrets
var username = fs.readFileSync(process.env.MONGO_USERNAME_FILE, 'utf8');
var password = fs.readFileSync(process.env.MONGO_PASSWORD_FILE, 'utf8');
print(username);
print(password);

// Create a new user for the specified database
db.getSiblingDB('iswars').createUser({
    user: username,
    pwd: password,
    roles: [{ role: 'readWrite', db: 'iswars' }]
});

quit();

print('User created...');
print('#---------------------------------------------');