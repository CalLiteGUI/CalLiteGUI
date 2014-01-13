#     Name: StudyTab.py
#   Author: Ben Tustison
#   E-mail: tustison@mbkengineers.com
#    Phone: 916.456.4400
# Last Rev: 06.25.2010
#  Purpose: Mimics StudyTab class from CALSIM

# python modules
import shutil
import os

#CalsimWsiDi class imports
from WsiDiGen import *
from vista.gui import *

# java class imports - standard
from java.awt import *
from java.awt.event import *
from java.io import *
from java.util import *
from javax.swing import *
from java.lang import *

import subprocess
from scripts.wrims2 import Utils

thisFileDir = os.path.dirname(os.path.realpath(__file__))
tab = "   "

# StudyTabCl class
class StudyTabCl:
   
   # constructor: initialize class parameters
   def __init__(self, configPath, iterations):

      self.configPath = configPath
      self.iterations = int(iterations)
      print "Config Path: "+ self.configPath
      print "Iterations : "+ str(self.iterations)
   
      try:
        self._cMap=Utils.getConfigMap(configPath)
        self.dvarFile=self._cMap.get("DvarFile")
        self.dvarFile=self.dvarFile.replace('\"','').replace('\'','')
      except:
        print '# Error in parsing config file: '+configPath

      
      if ":" not in self.dvarFile:
        self.dvarFile = os.path.join(os.path.dirname(self.configPath), self.dvarFile)
        print "dvarFile: " + self.dvarFile

      self.f=open(os.path.join(thisFileDir, "_wsidi_study.bat"),'w')

      self.f.write(r'%~dp0..\runConfig_calgui '+self.configPath+'\n')
      self.f.close()
            


      # assign other class variables
      self.report = True

      #These are user options found in the WRIMS GUI.
      self.hideWarnings = True
      self.hideProgressDetails = False

   ### FUNCTIONS

   # function to run CALSIM
   def execute(self):
      print tab+ "Running Model"
   
      #os.system('WRIMSv2_Engine.bat')
      subprocess.call("cmd /c start "+os.path.join(thisFileDir, "_wsidi_study.bat"))
      return 0

   # run WSI-DI procedure
   def runForWsi(self,crvName,crvWsiVar,crvDiVar,crvMax):

      studyDvName = self.dvarFile
      print "Set parameters"

      #newPath = os.path.join('../bin/','')
      #os.chdir(newPath)

      # number of Calsim runs needed (convergence)
      numRun = self.iterations

      # establish variable for WsiDiGenerator (WRIMS java class)
      wd = []

      # instantiate WsiDiGenerator for each curve to be generated.
      for i in range(len(crvName)):
         wd.append(WsiDiGenCl(self.configPath,crvName[i],crvWsiVar[i],crvDiVar[i],crvMax[i],studyDvName))

      print "finished set parameters"
      
      # run CALSIM and extract wsi-di data points for curve generation
      for i in range(0,numRun):
         print "Start iteration " + str(i+1)
         wsidi_log=open(os.path.join(os.path.dirname(self.configPath), "run\\wsidi_iteration.log"),'w')
         wsidi_log.write('iteration '+ str(i+1) + '/' + str(numRun) +'\n') 
         wsidi_log.close()
         # save initial wsi-di tables.  Table will be appended with "_0" if it's the first guess.  "_1" if it's the first curve generated by WSI-DI and so on.
         for j in range(len(crvName)):
            tblName = wd[j].runDir + File.separator + "lookup" + File.separator + "wsi_di_" + wd[j].name.lower() + ".table"
            if (File(tblName).exists()):
                tblNameSave = tblName[:-6] + "_"+str(i)+tblName[-6:]
                shutil.copy(tblName,tblNameSave) #copy file

         # run CALSIM and check status
         if(self.execute() !=0):
            break

         # load wsi-di data and fit curve
         for j in range(len(crvName)):
            wd[j].load(studyDvName)
            wd[j].execute()

         print ""
         
         # save DSS as separate file name.  The DV file name is appended with the same integer as the WSI-DI curves upon which they depended.
         # For instance, the initial WSI-DI curves are saved as "_0".  The first iteration DV file is also labeled "_0".
         if (i<numRun-1):
            dvNameSave = studyDvName[:-4] + "_"+str(i)+studyDvName[-4:]
            shutil.copy(studyDvName,dvNameSave) #copy file
