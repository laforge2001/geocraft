from org.geocraft.core.common.model import AbstractModel
from org.geocraft.ui.ijython.model import ScriptImpl

"""
A command line version of the function
"""

def clip(horName, min, max, clipType, areaOfInterest, propertyName, setValue):
  """
      Clip an horizon.
      
      clip(horName, min, max, clipType, areaOfInterest, propertyName, setValue)
      
      Arguments:

      horName - Horizon to be clipped
        Variable Name example - "var2"
      min - Minimum value to clip horizon
      max - Maximum value to clip horizon
      clipType - Type of clip - 
        "Replace with Clip Limits",
        "Replace with Nulls", or 
        "Replace with Constant"
      areaOfInterest - optional area of interest in the horizon where clip may be performed
      propertyName - Name of new property name to create
      setValue - When ClipType equals "Replace with Constant" - A constant will be used to set out of range values 
  """
  ScriptImpl.runScript7("ClipScript", 7, horName, min, max, clipType, areaOfInterest, propertyName, setValue)
  

class Clip(AbstractModel):

  def __init__(self):
    print "instantiating Clip"
    return
