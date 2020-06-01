package edu.cuhk.csci3310.utrack

import com.google.firebase.Timestamp

// University model file
class Uni_model(
    Title: String,
    Description: String,
    Status: String,
    timeStamp: Timestamp,
    deadline: String,
    transType: String,
    transStatus: String,
    GREStatus: String,
    TOEFLStatus: String,
    Rec1Name: String,
    Rec2Name: String,
    Rec3Name: String,
    Rec1Status: String,
    Rec2Status: String,
    Rec3Status: String,
    Notes: String
) {

    var uniTitle: String = Title
    var uniProg: String = Description
    var status: String = Status
    var timestamp: Timestamp = timeStamp
    var deadline: String = deadline
    var transType: String = transType
    var transStatus: String = transStatus
    var GREStatus: String = GREStatus
    var TOEFLStatus: String = TOEFLStatus
    var Rec1Name: String = Rec1Name
    var Rec2Name: String = Rec2Name
    var Rec3Name: String = Rec3Name
    var Rec1Status: String = Rec1Status
    var Rec2Status: String = Rec2Status
    var Rec3Status: String = Rec3Status
    var Notes: String = Notes


    constructor() : this(
        "", "", "", Timestamp.now(), "", "", "", "", ""
        , "", "", "", "", "", "", ""
    )


}