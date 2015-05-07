from org.geocraft.core.common.model import AbstractModel
from org.geocraft.ui.ijython.model import ScriptImpl

"""
A command line version of the function
"""

def medianSmooth(horName, colFilterSize, rowFilterSize, filterSize, interpolateOption, mask, outputName):
  """
      Smooth a horizon.
      
      medianSmooth(horName, colFilterSize, rowFilterSize, filterSize, interpolateOption, mask, outputName)
      
      Arguments:

      horName - Horizon to be smoothed
        Variable Name example - "var2"
      colFilterSize - Filter width in columns
      rowFilterSize - Filter width in rows
      filterSize - Filter size (Square filter size - Set to 0, if columns & rows were specified)
                   (Use this filter size for the # of CDPs when using 2D data)
      interpolateOption - Interpolate Option - 
        "Non-Null Values Only",
        "Null Values Only", or 
        "All values"
      mask - mask horizon -
        Set to 0, to filter the entire horizon
        Filter the Input horizon where the mask grid is not null,
      outputName - Name of new property name to create
  """
  ScriptImpl.runScript7("MedianSmoothScript", 7, horName, colFilterSize, rowFilterSize, filterSize, interpolateOption, mask, outputName)
  

class MedianSmooth(AbstractModel):

  def __init__(self):
    print "instantiating MedianSmooth"
    return
