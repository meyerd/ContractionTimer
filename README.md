Contraction Timer tracks and stores all of your contraction data.
=================================================================


Project is set up for use in Eclipse, but should work in any Android development environment.


This version is a fork of the excellent original project. 
Changes from the original version:

* Removed tracking code through firebase.
* Added visualization
**The visualization has very crude customization options.
** You can look at contractions (Co), Averages (Avg) and Standard Deviations (Std. Dev.)
   in contraction timings
** Prediciton: this is a least squares fit to the data (either contractions, averages or
   standard deviations). The idea is to fit a curve (n-order polynomial or other), to the
   standard deviations and if those tend to go to zero, then the birth time comes closer.
** Choose some predictions in advance to the data to see the prediction of birth time when
   the prediction line fitted to the std. dev. crosses zero