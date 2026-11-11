import numpy as np
import torch
from torch import nn


def encode_states(states, config, device):
    return (torch.as_tensor(np.stack([s.continuous(config) for s in states]), device=device),
            torch.tensor([s.position for s in states], dtype=torch.long, device=device),
            torch.tensor([s.profile for s in states], dtype=torch.long, device=device))


class HierarchicalQNetwork(nn.Module):


    def __init__(self, config, hidden_dim=256, embedding_dim=8):
        super().__init__()
        self.node_embedding = nn.Embedding(config.num_courses, embedding_dim)
        self.profile_embedding = nn.Embedding(3, embedding_dim)
        self.encoder = nn.Sequential(
            nn.Linear(config.continuous_dim + 2 * embedding_dim, hidden_dim), nn.ReLU(),
            nn.Linear(hidden_dim, hidden_dim), nn.ReLU())
        self.macro_head = nn.Linear(hidden_dim, 3)
        self.micro_head = nn.Linear(hidden_dim, 3 * 9)

    def forward(self, continuous, position, profile):
        features = torch.cat((continuous, self.node_embedding(position),
                              self.profile_embedding(profile)), dim=-1)
        hidden = self.encoder(features)
        macro = self.macro_head(hidden).unsqueeze(-1)
        micro = self.micro_head(hidden).reshape(-1, 3, 9)
        return (macro + micro).reshape(-1, 27)
