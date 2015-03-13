#!/usr/local/bin/python
"""
patient class: config_dir and jars_dir
"""
import os
import ConfigParser

configFilePath = './health_care.cfg'
configs = ConfigParser.RawConfigParser()
configs.read(configFilePath)
config_dir = configs.get('PATH', 'config_dir')
jars_dir = configs.get('PATH', 'jars_dir')

###################################
# configuration on patient's app  #
###################################
print "Configuring patient app"
patient_dir = './apps/patient/'

patient_config_dir = patient_dir + 'config_dir' 
if os.path.exists(patient_config_dir):
    print "config_dir exists and it will be rewritten"
    os.remove(patient_config_dir)

with open(patient_config_dir, 'w') as f:
    f.write(config_dir)

patient_jars_dir = patient_dir + 'jars_dir'
if os.path.exists(patient_jars_dir):
    print "jars_dir exists and it will be rewritten"
    os.remove(patient_jars_dir)

with open(patient_jars_dir, 'w') as f:
    f.write(jars_dir)

###################################
# configuration on doctor's app   #
###################################
print "Configuring doctor app"
doctor_dir = './apps/doctor/'

doctor_jars_dir = doctor_dir + 'jars_dir'
if os.path.exists(doctor_jars_dir):
    print "jars_dir exists and it will be rewritten"
    os.remove(doctor_jars_dir)

with open(doctor_jars_dir, 'w') as f:
    f.write(jars_dir)
