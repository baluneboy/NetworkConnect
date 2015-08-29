# for dHost and dKu fields, introduce units

CHANGE FROM THIS (WHERE UNITS ARE ASSUMED SEC)

DOY:hh:mm:ss  dHost    dKu  device
----------------------------------
241:11:30:00 1234.0 1234.0  phone

CHANGE TO THIS (WHERE UNITS ARE SUFFIX)

DOY:hh:mm:ss  dHost    dKu  device
----------------------------------
241:11:30:00   -59s     0s  phone
241:11:30:00   +59m     0s  phone
241:11:30:00   -23h     0s  phone
241:11:30:00  +364d     0s  phone
241:11:30:00   -15y     0s  phone