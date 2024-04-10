package at.ac.uibk.dps.cirrina.runtime.data;

public class DefaultDescriptions {

  public static String pingPong = """
        {
          name: 'collaborativeStateMachine',
          version: '0.1',
          memoryMode: 'shared',
          stateMachines: [
            {
              name: 'stateMachine1',
              states: [
                {
                  name: 'a',
                  initial: true,
                  entry: [
                    {
                      type: 'assign',
                      variable: {
                        name: 'v',
                        value: 'v + 1'
                      }
                    },
                    {
                      type: 'raise',
                      event: {
                        name: 'e1',
                        channel: 'internal'
                      }
                    }
                  ],
                  on: [
                    {
                      event: 'e2',
                      target: 'b'
                    }
                  ]
                },
                {
                  name: 'b',
                  entry: [
                    {
                      type: 'raise',
                      event: {
                        name: 'e3',
                        channel: 'internal'
                      }
                    }
                  ],
                  on: [
                    {
                      event: 'e4',
                      target: 'a'
                    }
                  ],
                  always: [
                    {
                      target: 'c',
                      guards: [
                        {
                          expression: "v >= 100"
                        }
                      ]
                    }
                  ]
                },
                {
                  name: 'c',
                  terminal: true
                }
              ]
            },
            {
              name: 'stateMachine2',
              states: [
                {
                  name: 'a',
                  initial: true,
                  entry: [
                    {
                      type: 'raise',
                      event: {
                        name: 'e4',
                        channel: 'internal'
                      }
                    }
                  ],
                  on: [
                    {
                      event: 'e1',
                      target: 'b'
                    }
                  ]
                },
                {
                  name: 'b',
                  entry: [
                    {
                      type: 'assign',
                      variable: {
                        name: 'v',
                        value: 'v + 1'
                      }
                    },
                    {
                      type: 'raise',
                      event: {
                        name: 'e2',
                        channel: 'internal'
                      }
                    }
                  ],
                  on: [
                    {
                      event: 'e3',
                      target: 'a'
                    }
                  ],
                  always: [
                    {
                      target: 'c',
                      guards: [
                        {
                          expression: "v >= 100"
                        }
                      ]
                    }
                  ]
                },
                {
                  name: 'c',
                  terminal: true
                }
              ]
            }
          ]
        }
      """;
}
