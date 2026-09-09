plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("agentContract") {
            id = "dev.charter.agent.contract"
            implementationClass = "dev.charter.agentcontract.AgentContractPlugin"
        }
    }
}
