rootProject.name = "payment-authorize-v1"

include("postgres")
include("postgres:banking", "postgres:banking:issuer")
include("postgres:compliance-regulatory", "postgres:compliance-regulatory:payment")
include("postgres:fraud-risk", "postgres:fraud-risk:payment")
include("postgres:payment", "postgres:payment:gateway", "postgres:payment:processor", "postgres:payment:card-network")
include("postgres:shopping", "postgres:shopping:merchant")

