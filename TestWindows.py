import os
import subprocess

for i in range(1001, 1007):
  dirName = r"C:\Users\Satvik\OneDrive - University of Florida\College\Fall 2021\CNT\Project\CNT4007-Project\large_local"
  cmd = "cd "+dirName+" && dir"
  print(dirName)
  # os.system("start /wait cmd /c cd "+cmd)
  p = subprocess.Popen(["java peerProcess", str(i)], cwd=dirName)
