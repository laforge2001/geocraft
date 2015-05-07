from org.geocraft.core.common.model import AbstractModel
from org.geocraft.ui.ijython.model import ScriptImpl

"""
A command line version of the function
"""

def weightedSmooth(horName, colFilterSize, rowFilterSize, edgeWeight, applyBlending, mask, outputName):
  """
      Smooth a horizon.
      
      weightedSmooth(horName, colFilterSize, rowFilterSize, edgeWeight, applyBlending, mask, outputName)
      
      Arguments:

      horNamePy - Horizon to be smoothed
        Variable Name example - "var2"
      colFilterSizePy - Filter width in columns
      rowFilterSizePy - Filter width in rows
      edgeWeight - Edge Weight (From 0 to 1 - 
        A Taper is performed so that points further away from the point being smoothed are given less weight)
      applyBlending - set to "true" to smooth the grid lines when using the mask horizon
      maskPy - mask horizon -
        Set to 0, to filter the entire horizon
        Filter the Input horizon where the mask grid is not null,
      outputNamePy - Name of new property name to create
  """
  ScriptImpl.runScript7("WeightedSmoothScript", 7, horName, colFilterSize, rowFilterSize, edgeWeight, applyBlending, mask, outputName)
  

class WeightedSmooth(AbstractModel):

  def __init__(self):
    print "instantiating WeightedSmooth"
    return
